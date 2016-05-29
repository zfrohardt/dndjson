import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.Scanner;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class MarkdownToJSONConverter {
    public static final String CASTING_TIME = "Casting Time";
    public static final String CASTING_TIME_ENGLISH = "English";
    public static final String CASTING_TIME_SECONDS = "Seconds";
    public static final String CLASSES = "Classes";
    public static final String COMPONENTS = "Components";
    public static final String COMPONENTS_ENGLISH = "English";
    public static final String COMPONENTS_MATERIAL = "M";
    public static final String COMPONENTS_MATERIAL_DESCRIPTION = "Material Description";
    public static final String COMPONENTS_SOMATIC = "S";
    public static final String COMPONENTS_VERBAL = "V";
    public static final String DATE = "Date";
    public static final String DURATION = "Duration";
    public static final String DURATION_CONCENTRATION = "Concentration";
    public static final String DURATION_ENGLISH = "English";
    public static final String DURATION_SECONDS = "Seconds";
    public static final String EFFECT = "Effect";
    public static final String LEVEL = "Level";
    public static final String NAME = "Name";
    public static final String RANGE = "Range";
    public static final String RANGE_ENGLISH = "English";
    public static final String RANGE_DISTANCE_FEET = "Distance";
    public static final String RITUAL = "Ritual";
    public static final String SCHOOL = "School";
    public static final String SOURCE = "Source";
    public static final String SOURCE_RESOURCE = "Resource";
    public static final String SOURCE_LOCATION = "Location";

    private static final String[] CLASS_LIST = { "bard", "cleric", "druid", "paladin", "ranger", "sorcerer", "warlock",
            "wizard" };

    private static final String CANTRIP_IDENTIFIER = "cantrip";
    private static final String RITUAL_IDENTIFIER = "(ritual)";

    private static final String ACTION_IDENTIFIER = "action";
    private static final String BONUS_ACTION_IDENTIFIER = "bonus action";
    private static final String DAY_IDENTIFIER = "day";
    private static final String HOUR_IDENTIFIER = "hour";
    private static final String MINUTE_IDENTIFIER = "minute";
    private static final String REACTION_IDENTIFIER = "reaction";
    private static final String ROUND_IDENTIFIER = "round";
    private static final String SECOND_IDENTIFIER = "second";

    private static final int ACTION_TIME_SECONDS = 5;
    private static final int BONUS_ACTION_TIME_SECONDS = 1;
    private static final int DAY_TIME_SECONDS = 86400;
    private static final int HOUR_TIME_SECONDS = 3600;
    private static final int MINUTE_TIME_SECONDS = 60;
    private static final int REACTION_TIME_SECONDS = 1;
    private static final int ROUND_TIME_SECONDS = 6;
    private static final int SECOND_TIME_SECONDS = 1;

    private static final String SELF_IDENTIFIER = "self";
    private static final String SIGHT_IDENTIFIER = "sight";
    private static final String SPECIAL_IDENTIFIER = "special";
    private static final String TOUCH_IDENTIFIER = "touch";
    private static final String UNLIMITED_IDENTIFIER = "unlimited";

    private static final int SELF_DISTANCE = 0;
    private static final int SIGHT_DISTANCE = Integer.MAX_VALUE;
    private static final int SPECIAL_DISTANCE = Integer.MAX_VALUE;
    private static final int TOUCH_DISTANCE = 5;
    private static final int UNLIMITED_DISTANCE = Integer.MAX_VALUE;

    private static final String MATERIAL_IDENTIFIER = "M";
    private static final String SOMATIC_IDENTIFIER = "S";
    private static final String VERBAL_IDENTIFIER = "V";

    private static final String INSTANTANEOUS_IDENTIFIER = "instantaneous";
    private static final String UNTIL_DISPELLED_IDENTIFIER = "until dispelled";

    private static final int INSTANTANEOUS_TIME_SECONDS = 0;
    private static final int SPECIAL_TIME_SECONDS = Integer.MAX_VALUE;
    private static final int UNTIL_DISPELLED_TIME_SECONDS = Integer.MAX_VALUE;

    public static final String MARKDOWN_DIRECTORY = "src/spells/markdown/";
    public static final String JSON_DIRECTORY = "src/spells/convertedjson/";

    public static void main(String[] args) throws FileNotFoundException, UnsupportedEncodingException {
        File markdownDirectory = new File(MARKDOWN_DIRECTORY);
        File[] markdownFiles = markdownDirectory.listFiles();

        for (File markdownFile : markdownFiles) {
            System.out.println(markdownFile.getName());
            JSONObject json = parseMarkdownFileToJSON(markdownFile);
            exportJSONToFile(json);
        }
    }

    private static JSONObject parseMarkdownFileToJSON(File markdownFile) throws FileNotFoundException {
        JSONObject jsonRepresentation = new JSONObject();
        Scanner fileReader = new Scanner(markdownFile);
        parseHeaderToJSON(fileReader, jsonRepresentation);
        parseBodyToJSON(fileReader, jsonRepresentation);
        return jsonRepresentation;
    }

    @SuppressWarnings("unchecked")
    private static void parseHeaderToJSON(Scanner fileReader, JSONObject json) {
        fileReader.nextLine();
        fileReader.nextLine();
        String title = fileReader.nextLine();
        json.put(NAME, title.substring(title.indexOf("\"") + 1, title.length() - 1));
        String date = fileReader.nextLine();
        json.put(DATE, date.substring(date.length() - 10));
        String[] source = fileReader.nextLine().substring(8).split("[.]");
        JSONObject sourceJson = new JSONObject();
        sourceJson.put(SOURCE_RESOURCE, source[0]);
        sourceJson.put(SOURCE_LOCATION, source[1]);
        json.put(SOURCE, sourceJson);
        String tags = fileReader.nextLine().substring(6);
        JSONArray classes = parseTagsToClasses(tags);
        json.put(CLASSES, classes);
        fileReader.nextLine();
    }

    @SuppressWarnings("unchecked")
    private static JSONArray parseTagsToClasses(String str) {
        JSONArray array = new JSONArray();
        String removedBrackets = str.substring(1, str.length() - 1);
        String[] elements = removedBrackets.split(", ");
        for (int i = 0; i < elements.length - 1; i++) {
            assert CLASS_LIST.toString().contains(elements[i]);
            array.add(elements[i]);
        }
        return array;
    }

    @SuppressWarnings("unchecked")
    private static void parseBodyToJSON(Scanner fileReader, JSONObject json) {
        fileReader.nextLine();
        parseLevelSchoolAndRitual(fileReader.nextLine().toLowerCase(), json);
        fileReader.nextLine();
        json.put(CASTING_TIME, parseCastingTime(fileReader.nextLine().toLowerCase()));
        fileReader.nextLine();
        json.put(RANGE, parseRange(fileReader.nextLine().toLowerCase()));
        fileReader.nextLine();
        json.put(COMPONENTS, parseComponents(fileReader.nextLine()));
        fileReader.nextLine();
        json.put(DURATION, parseDuration(fileReader.nextLine().toLowerCase()));
        fileReader.nextLine();
        json.put(EFFECT, parseEffect(fileReader));
    }

    @SuppressWarnings("unchecked")
    private static void parseLevelSchoolAndRitual(String line, JSONObject json) {
        String[] valuesArray = line.substring(2, line.length() - 2).split(" ");
        if (line.contains(CANTRIP_IDENTIFIER)) {
            json.put(LEVEL, valuesArray[1]);
            json.put(SCHOOL, valuesArray[0]);
            json.put(RITUAL, false);
        } else {
            json.put(LEVEL, valuesArray[0].substring(0, 0));
            json.put(SCHOOL, valuesArray[1]);
            if (valuesArray.length == 3) {
                assert line.contains(RITUAL_IDENTIFIER);
                json.put(RITUAL, true);
            } else {
                json.put(RITUAL, false);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private static JSONObject parseCastingTime(String line) {
        JSONObject castTimeJson = new JSONObject();
        String castTimeEnglish = line.split(": ")[1];
        castTimeJson.put(CASTING_TIME_ENGLISH, castTimeEnglish);
        String[] castTimePieces = castTimeEnglish.split(" ", 2);
        int castTimeBase = Integer.parseInt(castTimePieces[0]);
        String castTimeUnit = castTimePieces[1];
        int castTimeSeconds = convertToSeconds(castTimeBase, castTimeUnit);
        castTimeJson.put(CASTING_TIME_SECONDS, castTimeSeconds);
        return castTimeJson;
    }

    private static int convertToSeconds(int baseNum, String timeUnit) {
        if (timeUnit.contains(REACTION_IDENTIFIER)) {
            return baseNum * REACTION_TIME_SECONDS;
        } else if (timeUnit.contains(BONUS_ACTION_IDENTIFIER)) {
            return baseNum * BONUS_ACTION_TIME_SECONDS;
        } else if (timeUnit.contains(ACTION_IDENTIFIER) || timeUnit.contains("action or ")) {
            return baseNum * ACTION_TIME_SECONDS;
        } else if (timeUnit.contains(ROUND_IDENTIFIER) || timeUnit.contains(ROUND_IDENTIFIER + "s")) {
            return baseNum * ROUND_TIME_SECONDS;
        } else if (timeUnit.contains(SECOND_IDENTIFIER) || timeUnit.contains(SECOND_IDENTIFIER + "s")) {
            return baseNum * SECOND_TIME_SECONDS;
        } else if (timeUnit.contains(MINUTE_IDENTIFIER) || timeUnit.contains(MINUTE_IDENTIFIER + "s")) {
            return baseNum * MINUTE_TIME_SECONDS;
        } else if (timeUnit.contains(HOUR_IDENTIFIER) || timeUnit.contains(HOUR_IDENTIFIER + "s")) {
            return baseNum * HOUR_TIME_SECONDS;
        } else if (timeUnit.contains(DAY_IDENTIFIER) || timeUnit.contains(DAY_IDENTIFIER + "s")) {
            return baseNum * DAY_TIME_SECONDS;
        } else {
            throw new RuntimeException("Unable to identify time factor: " + timeUnit);
        }
    }

    @SuppressWarnings("unchecked")
    private static JSONObject parseRange(String line) {
        JSONObject rangeJson = new JSONObject();
        String rangeEnglish = line.split(": ")[1];
        rangeJson.put(RANGE_ENGLISH, rangeEnglish);
        if (rangeEnglish.contains(SELF_IDENTIFIER)) {
            rangeJson.put(RANGE_DISTANCE_FEET, SELF_DISTANCE);
        } else if (rangeEnglish.contains(TOUCH_IDENTIFIER)) {
            rangeJson.put(RANGE_DISTANCE_FEET, TOUCH_DISTANCE);
        } else if (rangeEnglish.contains(SIGHT_IDENTIFIER)) {
            rangeJson.put(RANGE_DISTANCE_FEET, SIGHT_DISTANCE);
        } else if (rangeEnglish.contains(SPECIAL_IDENTIFIER)) {
            rangeJson.put(RANGE_DISTANCE_FEET, SPECIAL_DISTANCE);
        } else if (rangeEnglish.contains(UNLIMITED_IDENTIFIER)) {
            rangeJson.put(RANGE_DISTANCE_FEET, UNLIMITED_DISTANCE);
        } else {
            String[] rangeComponents = rangeEnglish.split(" ");
            assert rangeComponents[1].contains("feet");
            rangeJson.put(RANGE_DISTANCE_FEET, Integer.parseInt(rangeComponents[0]));
        }
        return rangeJson;
    }

    @SuppressWarnings("unchecked")
    private static JSONObject parseComponents(String line) {
        JSONObject componentsJson = new JSONObject();
        String componentsEnglish = line.split(": ")[1];
        componentsJson.put(COMPONENTS_ENGLISH, componentsEnglish);
        componentsJson.put(COMPONENTS_VERBAL, componentsEnglish.contains(VERBAL_IDENTIFIER));
        componentsJson.put(COMPONENTS_SOMATIC, componentsEnglish.contains(SOMATIC_IDENTIFIER));
        boolean containsMaterial = componentsEnglish.contains(MATERIAL_IDENTIFIER);
        componentsJson.put(COMPONENTS_MATERIAL, containsMaterial);
        String materialDescription;
        if (containsMaterial) {
            materialDescription = componentsEnglish.substring(componentsEnglish.indexOf("(") + 1,
                    componentsEnglish.indexOf(")"));
        } else {
            materialDescription = "None";
        }
        componentsJson.put(COMPONENTS_MATERIAL_DESCRIPTION, materialDescription);
        return componentsJson;
    }

    @SuppressWarnings("unchecked")
    private static JSONObject parseDuration(String line) {
        JSONObject durationJson = new JSONObject();
        String durationEnglish = line.split(": ")[1];
        durationJson.put(DURATION_ENGLISH, durationEnglish);
        durationJson.put(DURATION_CONCENTRATION, durationEnglish.contains("concentration"));
        if (durationEnglish.contains(INSTANTANEOUS_IDENTIFIER)) {
            durationJson.put(DURATION_SECONDS, INSTANTANEOUS_TIME_SECONDS);
        } else if (durationEnglish.contains(SPECIAL_IDENTIFIER)) {
            durationJson.put(DURATION_SECONDS, SPECIAL_TIME_SECONDS);
        } else if (durationEnglish.contains(UNTIL_DISPELLED_IDENTIFIER)) {
            durationJson.put(DURATION_SECONDS, UNTIL_DISPELLED_TIME_SECONDS);
        } else {
            String[] durationParts = durationEnglish.split(" ");
            int durationBaseTime = Integer.parseInt(durationParts[durationParts.length - 2]);
            String durationTimeUnit = durationParts[durationParts.length - 1];
            durationJson.put(DURATION_SECONDS, convertToSeconds(durationBaseTime, durationTimeUnit));
        }
        return durationJson;
    }

    private static String parseEffect(Scanner fileReader) {
        String effect = "\t";
        while (fileReader.hasNextLine()) {
            effect += fileReader.nextLine() + "\n";
        }
        return effect.replaceAll("\n\n", "\n\t").replaceAll("[*][*]", "");
    }

    private static void exportJSONToFile(JSONObject json) throws FileNotFoundException, UnsupportedEncodingException {
        String spellName = (String) json.get(NAME);
        String fileName = JSON_DIRECTORY;
        fileName += spellName.replaceAll(" ", "_").replaceAll("[/\\\\]", "-");
        fileName += ".json";
        PrintWriter writer = new PrintWriter(fileName, "UTF-8");
        writer.println(json.toJSONString());
        writer.close();
    }
}
