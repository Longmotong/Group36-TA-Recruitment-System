package MO_system.skill;

import MO_system.DataRoot;

import com.google.gson.Gson;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

/**
 * MO job posting skill catalog (reads {@code data/ta_skill_pool.json}).
 */
public final class MoSkillCatalog {
    private static final String FILE_NAME = "ta_skill_pool.json";
    private static final Gson GSON = new Gson();
    private static volatile CatalogData cached;

    private MoSkillCatalog() {
    }

    public static List<UiGroup> groups() {
        return get().groups;
    }

    public static List<String> allSkillNamesOrdered() {
        return get().allSkillNames;
    }

    private static CatalogData get() {
        CatalogData data = cached;
        if (data == null) {
            synchronized (MoSkillCatalog.class) {
                data = cached;
                if (data == null) {
                    cached = data = loadFromDisk();
                }
            }
        }
        return data;
    }

    private static CatalogData loadFromDisk() {
        Path file = resolveFile();
        if (file != null && Files.isRegularFile(file)) {
            try (Reader reader = Files.newBufferedReader(file)) {
                RawCatalog raw = GSON.fromJson(reader, RawCatalog.class);
                if (raw != null && raw.groups != null && !raw.groups.isEmpty()) {
                    return CatalogData.from(raw);
                }
            } catch (IOException ex) {
                System.err.println("[MoSkillCatalog] Failed to load " + file + ": " + ex.getMessage());
            }
        }
        return CatalogData.fromDefaults();
    }

    private static Path resolveFile() {
        Path primary = DataRoot.resolve().resolve(FILE_NAME);
        if (Files.isRegularFile(primary)) {
            return primary;
        }
        Path fallback = Path.of("data", FILE_NAME).toAbsolutePath().normalize();
        return Files.isRegularFile(fallback) ? fallback : null;
    }

    private static final class RawCatalog {
        List<RawGroup> groups;
    }

    private static final class RawGroup {
        String title;
        String description;
        String iconLabel;
        List<RawSubGroup> subGroups;
    }

    private static final class RawSubGroup {
        String title;
        List<String> skills;
    }

    private static final class CatalogData {
        final List<UiGroup> groups;
        final List<String> allSkillNames;

        CatalogData(List<UiGroup> groups, List<String> allSkillNames) {
            this.groups = groups;
            this.allSkillNames = allSkillNames;
        }

        static CatalogData from(RawCatalog raw) {
            List<UiGroup> groups = new ArrayList<>();
            LinkedHashSet<String> names = new LinkedHashSet<>();
            for (RawGroup rg : raw.groups) {
                List<UiSubGroup> subs = new ArrayList<>();
                if (rg.subGroups != null) {
                    for (RawSubGroup rsg : rg.subGroups) {
                        List<String> skills = rsg.skills != null ? List.copyOf(rsg.skills) : List.of();
                        subs.add(new UiSubGroup(
                                rsg.title != null ? rsg.title : "",
                                skills));
                        names.addAll(skills);
                    }
                }
                groups.add(new UiGroup(
                        rg.title != null ? rg.title : "",
                        rg.description != null ? rg.description : "",
                        rg.iconLabel != null ? rg.iconLabel : "",
                        List.copyOf(subs)));
            }
            return new CatalogData(List.copyOf(groups), List.copyOf(names));
        }

        static CatalogData fromDefaults() {
            RawCatalog raw = new RawCatalog();
            raw.groups = new ArrayList<>();
            RawGroup tech = new RawGroup();
            tech.title = "Technical Skills";
            tech.description = "Select skills and set your proficiency level for each competency";
            tech.iconLabel = "</>";
            tech.subGroups = List.of(
                    subGroup("Programming Languages",
                            "Java", "Python", "C/C++", "SQL", "Algorithms & Data Structures",
                            "Object-Oriented Programming (OOP)"),
                    subGroup("Hardware & Logic Design",
                            "VHDL", "Verilog", "Digital Logic Design", "FPGA Development & Debugging"),
                    subGroup("Embedded Systems",
                            "STM32 Development", "FreeRTOS", "Embedded C", "Hardware Driver Development"));
            raw.groups.add(tech);
            RawGroup tools = new RawGroup();
            tools.title = "Software & Engineering Tools";
            tools.description = "Select tools and set your proficiency level for each one";
            tools.iconLabel = "T";
            tools.subGroups = List.of(subGroup("Professional Development & Simulation Tools",
                    "Quartus Prime", "Keil5", "STM32CubeIDE", "STM32CubeMX",
                    "CST Studio Suite", "Matlab / Simulink", "Cisco Packet Tracer"));
            raw.groups.add(tools);
            RawGroup lang = new RawGroup();
            lang.title = "Language Proficiency";
            lang.description = "Set your English language proficiency level";
            lang.iconLabel = "L";
            lang.subGroups = List.of(subGroup("English", "English"));
            raw.groups.add(lang);
            return from(raw);
        }

        private static RawSubGroup subGroup(String title, String... skills) {
            RawSubGroup sg = new RawSubGroup();
            sg.title = title;
            sg.skills = List.of(skills);
            return sg;
        }
    }

    public record UiGroup(String title, String description, String iconLabel, List<UiSubGroup> subGroups) {
    }

    public record UiSubGroup(String title, List<String> skills) {
    }
}
