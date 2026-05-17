#!/usr/bin/env python3
"""
sync-agent-docs.py — Sincronización Automática de Documentación para Agentes IA.

Escanea las capas domain/, application/ e infrastructure/ del proyecto,
extrae clases, interfaces, enums, records y métodos públicos principales,
y verifica que estén documentados en los archivos .md del directorio
Externo/Agent/Project/. Inyecta un anexo de "Estado de Archivos Actual"
al final de cada documento.

Uso:
    python sync-agent-docs.py              # Modo escaneo + reporte
    python sync-agent-docs.py --check       # Exit code 1 si hay clases sin documentar
    python sync-agent-docs.py --update      # Actualiza los anexos en los .md
"""

import ast
import os
import re
import sys
from datetime import datetime, timezone
from pathlib import Path
from typing import Dict, List, NamedTuple, Set, Tuple

# Forzar UTF-8 en Windows para imprimir caracteres especiales
if sys.platform == "win32":
    sys.stdout.reconfigure(encoding="utf-8")  # type: ignore[attr-defined]

# ─── Configuración ──────────────────────────────────────────────────────────

BACKEND_DIR = Path(__file__).parent.resolve()
SRC_DIR = BACKEND_DIR / "src" / "main" / "java"
DOCS_DIR = BACKEND_DIR / "Externo" / "Agent" / "Project"
BASE_PACKAGE = "com.logistics.packages"

DOC_FILES_MAP = {
    "domain": DOCS_DIR / "01-Domain-Core.md",
    "application": DOCS_DIR / "02-Application-UseCases.md",
    "infrastructure": DOCS_DIR / "03-Infrastructure-Adapters.md",
}

LAYER_DIRS = {
    "domain": SRC_DIR / BASE_PACKAGE.replace(".", "/") / "domain",
    "application": SRC_DIR / BASE_PACKAGE.replace(".", "/") / "application",
    "infrastructure": SRC_DIR / BASE_PACKAGE.replace(".", "/") / "infrastructure",
}


# ─── Tipos ──────────────────────────────────────────────────────────────────

class JavaType(NamedTuple):
    kind: str          # "class", "interface", "enum", "record", "abstract_class"
    name: str
    package: str
    file_path: str
    methods: List[str]

class ScanResult(NamedTuple):
    types: List[JavaType]
    total_classes: int
    total_interfaces: int
    total_enums: int
    total_records: int
    total_methods: int


# ─── Escáner de Código Fuente ──────────────────────────────────────────────

def parse_java_file(filepath: Path) -> JavaType | None:
    """Analiza un archivo Java y extrae su estructura mediante regex."""
    try:
        content = filepath.read_text(encoding="utf-8")
    except Exception:
        return None

    # Extraer package
    pkg_match = re.search(r"^package\s+([\w.]+);", content, re.MULTILINE)
    if not pkg_match:
        return None
    package = pkg_match.group(1)

    # Detectar tipo de declaración
    kind = None
    name = None

    # Record
    record_match = re.search(
        r"(?:public\s+)?(?:abstract\s+)?record\s+(\w+)", content
    )
    if record_match:
        kind = "record"
        name = record_match.group(1)

    # Enum
    enum_match = re.search(r"(?:public\s+)?enum\s+(\w+)", content)
    if enum_match and kind is None:
        kind = "enum"
        name = enum_match.group(1)

    # Interface
    iface_match = re.search(r"(?:public\s+)?interface\s+(\w+)", content)
    if iface_match and kind is None:
        kind = "interface"
        name = iface_match.group(1)

    # Abstract class
    abs_match = re.search(r"(?:public\s+)?abstract\s+class\s+(\w+)", content)
    if abs_match and kind is None:
        kind = "abstract_class"
        name = abs_match.group(1)

    # Regular class
    cls_match = re.search(r"(?:public\s+)?(?:final\s+)?class\s+(\w+)", content)
    if cls_match and kind is None:
        kind = "class"
        name = cls_match.group(1)

    # Enum inside domain/valueobject (clase principal)
    if not kind:
        return None

    # Extraer métodos públicos (no constructores, no getters/setters triviales)
    methods = []
    method_pattern = re.compile(
        r"(?:public\s+)(?:\w+\s+)*?(\w+)\s*\([^)]*\)\s*(?:throws\s+\w+(?:,\s*\w+)*)?\s*\{",
        re.MULTILINE,
    )
    for m in method_pattern.finditer(content):
        method_name = m.group(1)
        # Filtrar constructores, getters, setters, builders, toString, equals, hashCode
        if method_name.startswith("get") and len(method_name) > 3 and method_name[3].isupper():
            continue
        if method_name.startswith("set") and len(method_name) > 3 and method_name[3].isupper():
            continue
        if method_name.startswith("is") and len(method_name) > 2 and method_name[2].isupper():
            continue
        if method_name in ("builder", "toString", "equals", "hashCode", "of", "prePersist"):
            continue
        if method_name.startswith("_") or method_name == name:
            continue
        methods.append(method_name)

    rel_path = str(filepath.relative_to(SRC_DIR))
    return JavaType(
        kind=kind, name=name, package=package,
        file_path=rel_path, methods=methods,
    )


def scan_directory(layer_name: str) -> ScanResult:
    """Escanea recursivamente un directorio y extrae todos los tipos Java."""
    root = LAYER_DIRS.get(layer_name)
    if not root or not root.exists():
        return ScanResult([], 0, 0, 0, 0, 0)

    types = []
    counts = {"class": 0, "interface": 0, "enum": 0, "record": 0, "abstract_class": 0}
    total_methods = 0

    for filepath in sorted(root.rglob("*.java")):
        result = parse_java_file(filepath)
        if result:
            types.append(result)
            if result.kind in counts:
                counts[result.kind] += 1
            total_methods += len(result.methods)

    return ScanResult(
        types=types,
        total_classes=counts["class"],
        total_interfaces=counts["interface"],
        total_enums=counts["enum"],
        total_records=counts["record"],
        total_methods=total_methods,
    )


# ─── Generador de Anexo ───────────────────────────────────────────────────

def generate_appendix(layer: str, scan: ScanResult) -> str:
    """Genera el bloque de anexo de estado actual para insertar en los .md."""
    now = datetime.now(timezone.utc).strftime("%Y-%m-%d %H:%M:%S UTC")

    summary = (
        f"\n\n---\n\n## Anexo: Estado de Archivos Actual ({layer})\n"
        f"*Generado automáticamente por sync-agent-docs.py el {now}*\n\n"
        f"| Indicador | Valor |\n"
        f"|---|---|\n"
        f"| Clases | {scan.total_classes} |\n"
        f"| Interfaces | {scan.total_interfaces} |\n"
        f"| Enumeraciones | {scan.total_enums} |\n"
        f"| Records | {scan.total_records} |\n"
        f"| Métodos públicos (significativos) | {scan.total_methods} |\n"
        f"| Archivos analizados | {len(scan.types)} |\n\n"
    )

    summary += "### Tipos Detectados\n\n"
    summary += "| Tipo | Nombre | Paquete | Métodos públicos |\n"
    summary += "|---|---|---|---|\n"

    for t in scan.types:
        kind_icon = {
            "class": "🟦 Cls",
            "interface": "🟩 Int",
            "enum": "🟨 Enm",
            "record": "🟪 Rec",
            "abstract_class": "🔷 Abs",
        }.get(t.kind, t.kind)

        methods_str = ", ".join(t.methods) if t.methods else "—"
        if len(methods_str) > 100:
            methods_str = methods_str[:97] + "..."

        summary += f"| {kind_icon} | `{t.name}` | `{t.package}` | `{methods_str}` |\n"

    return summary


# ─── Detector de Clases No Documentadas ────────────────────────────────────

def find_undocumented_classes(scan: ScanResult, md_content: str) -> List[str]:
    """Encuentra nombres de clases/records/interfaces/enums no mencionados en el .md."""
    undocumented = []
    for t in scan.types:
        if t.name not in md_content:
            undocumented.append(t.name)
    return undocumented


def _extract_backtick_names(text: str) -> set:
    """Extrae nombres PascalCase entre backticks, excluyendo cabeceras de tabla."""
    names = set()
    for match in re.finditer(r'`(\w+)`', text):
        name = match.group(1)
        if not name or not name[0].isupper():
            continue
        if name.isupper() and len(name) > 1:
            continue  # Acrónimos
        if name in {"Cls", "Int", "Enm", "Rec", "Abs", "Tipo",
                     "Nombre", "Paquete", "Métodos", "Públicos",
                     "Indicador", "Valor", "Tipos", "Detectados",
                     "Archivos", "Analizados"}:
            continue
        names.add(name)
    return names


def find_orphaned_classes(scan: ScanResult, md_content: str) -> List[str]:
    """Encuentra clases del proyecto que ya no existen en el código fuente.

    Solo reporta clases que aparecen TANTO en el anexo (prueba de que fueron
    tipos del proyecto) COMO en el cuerpo (aún referenciadas en la documentación).
    Esto evita falsos positivos con clases de frameworks externos.
    """
    current_names = {t.name for t in scan.types}

    appendix_marker = "## Anexo: Estado de Archivos Actual"
    idx = md_content.find(appendix_marker)
    if idx == -1:
        return []

    body = md_content[:idx]
    appendix = md_content[idx:]

    body_names = _extract_backtick_names(body)
    appendix_names = _extract_backtick_names(appendix)

    orphaned = []
    for name in sorted(appendix_names & body_names):  # Intersección = proyecto propio
        if name not in current_names:
            orphaned.append(name)

    return orphaned


def save_appendix(filepath: Path, appendix: str) -> bool:
    """Guarda o actualiza el anexo al final del archivo .md."""
    if not filepath.exists():
        return False

    content = filepath.read_text(encoding="utf-8")

    # Remover anexo anterior si existe
    appendix_marker = "\n## Anexo: Estado de Archivos Actual"
    idx = content.find(appendix_marker)
    if idx != -1:
        content = content[:idx].rstrip()

    # Agregar nuevo anexo
    content += appendix
    filepath.write_text(content, encoding="utf-8")
    return True


# ─── Main ──────────────────────────────────────────────────────────────────

def main():
    import argparse

    parser = argparse.ArgumentParser(
        description="sync-agent-docs.py — Sincronización de documentación para Agentes IA"
    )
    parser.add_argument(
        "--check", action="store_true",
        help="Verifica si hay clases sin documentar. Exit code 1 si hay errores."
    )
    parser.add_argument(
        "--update", action="store_true",
        help="Actualiza los anexos de estado en los archivos .md del proyecto."
    )
    args = parser.parse_args()

    all_ok = True
    results: Dict[str, ScanResult] = {}

    print("=" * 60)
    print("sync-agent-docs.py — Escaneo Estructural del Proyecto")
    print("=" * 60)

    for layer in ["domain", "application", "infrastructure"]:
        print(f"\n─── Escaneando {layer}/ ───")
        scan = scan_directory(layer)
        results[layer] = scan

        print(f"  Clases: {scan.total_classes}, Interfaces: {scan.total_interfaces}, "
              f"Enums: {scan.total_enums}, Records: {scan.total_records}")
        print(f"  Métodos públicos: {scan.total_methods}")
        print(f"  Total archivos: {len(scan.types)}")

        for t in scan.types:
            print(f"    {t.kind:15s} {t.name:30s} {t.file_path}")

        # Verificar documentación
        md_file = DOC_FILES_MAP.get(layer)
        if md_file and md_file.exists():
            md_content = md_file.read_text(encoding="utf-8")
            undocumented = find_undocumented_classes(scan, md_content)
            if undocumented:
                all_ok = False
                print(f"\n  ⚠️  CLASES NO DOCUMENTADAS en {md_file.name}:")
                for cls in undocumented:
                    print(f"      - {cls}")

            orphaned = find_orphaned_classes(scan, md_content)
            if orphaned:
                all_ok = False
                print(f"\n  ⚠️  CLASES ELIMINADAS aún referenciadas en {md_file.name}:")
                for cls in orphaned:
                    print(f"      - {cls}")

            if not undocumented and not orphaned:
                print(f"  ✅ Documentación alineada con el código en {md_file.name}")
        else:
            print(f"  ⚠️  Archivo de documentación no encontrado: {md_file}")

    # ─── Resumen ─────────────────────────────────────────────────────────
    print("\n" + "=" * 60)
    total_types = sum(len(r.types) for r in results.values())
    total_methods = sum(r.total_methods for r in results.values())
    print(f"RESUMEN: {total_types} tipos, {total_methods} métodos públicos escaneados")

    if args.update:
        print("\n─── Actualizando anexos en archivos .md ───")
        for layer, scan in results.items():
            md_file = DOC_FILES_MAP.get(layer)
            if not md_file:
                continue
            appendix = generate_appendix(layer, scan)
            if save_appendix(md_file, appendix):
                print(f"  ✅ Anexo actualizado en {md_file.name}")
            else:
                print(f"  ⚠️  No se pudo actualizar {md_file}")

    if args.check and not all_ok:
        print("\n❌ Se detectaron problemas de documentación (clases sin documentar o huérfanas).")
        sys.exit(1)
    elif args.check and all_ok:
        print("\n✅ Documentación alineada con el código.")
    else:
        print(f"\n{'⚠️  Hay problemas de documentación.' if not all_ok else '✅ Todo en orden.'}")

    # ─── Generar reporte JSON de estado ──────────────────────────────
    report_path = BACKEND_DIR / "Externo" / "Agent" / ".sync-status.json"
    import json

    report = {
        "timestamp": datetime.now(timezone.utc).isoformat(),
        "all_documented": all_ok,
        "layers": {}
    }
    for layer, scan in results.items():
        report["layers"][layer] = {
            "classes": scan.total_classes,
            "interfaces": scan.total_interfaces,
            "enums": scan.total_enums,
            "records": scan.total_records,
            "methods": scan.total_methods,
            "files": len(scan.types),
            "types": [
                {"kind": t.kind, "name": t.name, "package": t.package, "methods": len(t.methods)}
                for t in scan.types
            ]
        }

    report_path.parent.mkdir(parents=True, exist_ok=True)
    report_path.write_text(json.dumps(report, indent=2, default=str), encoding="utf-8")
    print(f"\n📊 Reporte JSON guardado en: {report_path}")

    return 0 if all_ok else 1


if __name__ == "__main__":
    sys.exit(main())
