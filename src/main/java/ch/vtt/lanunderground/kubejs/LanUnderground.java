package ch.vtt.lanunderground.kubejs;

import dev.latvian.mods.kubejs.KubeJSPlugin;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.security.CodeSource;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

public class LanUnderground extends KubeJSPlugin {

    private static final String RESOURCE_ROOT = "kubejs";

    @Override
    public void init() {
        ch.vtt.lanunderground.LanUnderground.LOGGER.info("kubejs startup...");

        // Zielverzeichnis: <minecraft>/kubejs
        Path gameDir = FabricLoader.getInstance().getGameDir();
        Path targetDir = gameDir.resolve("kubejs");

        try {
            extractResources(RESOURCE_ROOT, targetDir);
            modifyModernIndustrializationConfig(gameDir.resolve("config").resolve("modern_industrialization.toml"));
            ch.vtt.lanunderground.LanUnderground.LOGGER.info("KubeJS-Skripte erfolgreich extrahiert nach {}", targetDir);
        } catch (Exception e) {
            ch.vtt.lanunderground.LanUnderground.LOGGER.error("Fehler beim Extrahieren der KubeJS-Ressourcen:", e);
        }
    }

    /**
     * Extrahiert alle Dateien aus dem angegebenen Ordner in der JAR ins Zielverzeichnis.
     * Besteht das Ziel bereits, werden vorhandene Dateien nicht überschrieben.
     */
    private void extractResources(String resourceFolder, Path targetFolder) throws URISyntaxException, IOException {
        CodeSource src = getClass().getProtectionDomain().getCodeSource();
        if (src == null) {
            ch.vtt.lanunderground.LanUnderground.LOGGER.warn("Keine CodeSource gefunden, Extraktion übersprungen.");
            return;
        }

        Path jarPath = Paths.get(src.getLocation().toURI());
        FileSystem fs = null;
        Path jarRoot;

        if (Files.isDirectory(jarPath)) {
            // Entwicklungsumgebung: Ressourcen-Ordner direkt im Filesystem
            Path rootPath = Paths.get(".").resolve("../src/main").normalize().toAbsolutePath();
            jarRoot = rootPath.resolve("resources").resolve(resourceFolder);
        } else {
            // Produktions-JAR: erstelle oder hole ZIP-Dateisystem
            URI jarUri = URI.create("jar:" + jarPath.toUri().toString());
            try {
                fs = FileSystems.newFileSystem(jarUri, Collections.emptyMap());
            } catch (FileSystemAlreadyExistsException e) {
                fs = FileSystems.getFileSystem(jarUri);
            }
            jarRoot = fs.getPath("/" + resourceFolder);
        }

        try (Stream<Path> paths = Files.walk(jarRoot)) {
            paths.forEach(source -> {
                try {
                    Path relative = jarRoot.relativize(source);
                    Path destination = targetFolder.resolve(relative.toString());
                    if (Files.isDirectory(source)) {
                        if (Files.notExists(destination)) {
                            Files.createDirectories(destination);
                        }
                    } else {
                        if (Files.notExists(destination)) {
                            Files.createDirectories(destination.getParent());
                            Files.copy(source, destination);
                            ch.vtt.lanunderground.LanUnderground.LOGGER.debug("Kopiert {} -> {}", source, destination);
                        }
                    }
                } catch (IOException ex) {
                    ch.vtt.lanunderground.LanUnderground.LOGGER.error("Fehler beim Kopieren von {}:", source, ex);
                }
            });
        } finally {
            if (fs != null && fs.isOpen()) {
                fs.close();
            }
        }
    }

    /**
     * Setzt in der config/modern_industrialization.toml den Wert datagenOnStartup auf true.
     */
    private void modifyModernIndustrializationConfig(Path configPath) {
        try {
            if (Files.notExists(configPath)) {
                ch.vtt.lanunderground.LanUnderground.LOGGER.warn("Config-Datei nicht gefunden: {}", configPath);
                return;
            }
            List<String> lines = Files.readAllLines(configPath, StandardCharsets.UTF_8);
            boolean found = false;
            for (int i = 0; i < lines.size(); i++) {
                String line = lines.get(i);
                if (line.trim().startsWith("datagenOnStartup")) {
                    lines.set(i, "datagenOnStartup = true");
                    found = true;
                    break;
                }
            }
            if (!found) {
                lines.add("datagenOnStartup = true");
            }
            Files.write(configPath, lines, StandardCharsets.UTF_8, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING);
            ch.vtt.lanunderground.LanUnderground.LOGGER.info("Setze datagenOnStartup auf true in {}", configPath);
        } catch (IOException e) {
            ch.vtt.lanunderground.LanUnderground.LOGGER.error("Fehler beim Anpassen der config-Datei:", e);
        }
    }
}
