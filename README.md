# PF4J plugin development

### Run ABAC Application in Intellij Idea in PF4j development mode
1. Add VM options: `-Dpf4j.mode=development -Dpf4j.pluginsDir="C:\Users\User\projects\thinkabac"`
2. Create file `enabled.txt` in the project root directory. The content of file should contain only plugin name which is equal to module name, for example `abac-plugin-dummy`
3. Create appropriate values in `application.properties`, `abac-plugin-dummy.properties` and `keycloak.json`

# Rebuilding GUI

After modifications of better-abac-server/src/main/ui, rebuild the GUI:
- Install Node 11.x
- npm i -g gulp
- npm i -g bower
- cd better-abac-server/src/main/ui
- bower install
- gulp