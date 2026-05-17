# Commits prompt
```text
Analiza el registro de git (mediante `git diff` o el estado actual) para tener el contexto exacto de los archivos modificados y/o añadidos. En base a esto, genera un documento .md en la carpeta 'Backend/Externo/' con una propuesta de commits estructurada.
Para cada commit detectado, proporciona el output en un formato Markdown limpio y fácil de copiar, usando la siguiente estructura:
Archivos vinculados al commit, es decir, archivos a añadir mediante git add
'git commit -m "type(scope): title
Description"'

Types: Feature, Chore, Fix, Refactor, Tests, Style, etc.
Pero no quiero un solo commit, deberá ser una lista de commits segun su scope, en el output debes mostrar qué archivos o paquete de archivos serán vinculados a un commit, el mensaje de commit necesito que sea facil de copiar
El mensaje de commit deberá estar en español, a excepcion del type y scope.
```

# Pull Request prompt
```text
Crea una descripcion en formato md para el merge del feature a develop.
Ten en cuenta el registro de commits que generaste para la realizacion de dicha tarea.
Quiero que tengas en cuenta notaciones como .[!NOTE]. .[!IMPORTANT]. .[!TIP]. etc.
Me gustaría que hicieras descripciones detalladas sobre desiciones arquitectonicas y de tecnologias utilizadas.
En caso de que pienses crear una seccion de Registro de cambios (Commits), no lo hagas es innecesario. Cambialo por algo más práctico, github ya muestra eso de manera nativa.
```