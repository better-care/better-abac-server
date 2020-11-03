package care.better.abac.plugin;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

import org.apache.commons.io.FilenameUtils;
import org.pf4j.DefaultPluginDescriptor;
import org.pf4j.ManifestPluginDescriptorFinder;
import org.pf4j.Plugin;
import org.pf4j.PluginDescriptor;
import org.pf4j.PluginDescriptorFinder;
import org.pf4j.PluginRuntimeException;
import org.pf4j.util.FileUtils;

/**
 * @author Andrej Dolenc
 */
public class FilenamePluginDescriptionFinder implements PluginDescriptorFinder {

    @Override
    public boolean isApplicable(Path pluginPath) {
        if (Files.exists(pluginPath) && FileUtils.isJarFile(pluginPath)) {
            try (JarFile jarFile = new JarFile(pluginPath.toFile())) {
                Manifest manifest = jarFile.getManifest();
                return manifest == null || manifest.getMainAttributes().getValue(ManifestPluginDescriptorFinder.PLUGIN_ID) == null &&
                        manifest.getMainAttributes().getValue(ManifestPluginDescriptorFinder.PLUGIN_VERSION) == null;
            }
            catch (IOException e) {
                throw new PluginRuntimeException(e);
            }
        }
        return true;
    }

    @Override
    public PluginDescriptor find(Path pluginPath) {
        String name = FilenameUtils.getBaseName(pluginPath.toFile().getName());
        return new DefaultPluginDescriptor(name, null, Plugin.class.getName(), "*", "*", null, null);
    }
}
