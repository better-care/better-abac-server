# PF4J plugin development

### Run ABAC Application in Intellij Idea in PF4j development mode
1. Add VM options: `-Dpf4j.mode=development -Dpf4j.pluginsDir="C:\Users\User\projects\thinkabac"`
2. Create file `enabled.txt` in the project root directory. The content of file should contain only plugin name which is equal to module name, for example `better-abac-plugin-dummy-server`
3. Create appropriate values in `application.properties`, `better-abac-plugin-dummy-server.properties` and `keycloak.json`