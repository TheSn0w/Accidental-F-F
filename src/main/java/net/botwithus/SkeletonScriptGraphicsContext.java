package net.botwithus;

import net.botwithus.SkeletonScript;
import net.botwithus.rs3.game.skills.Skills;
import net.botwithus.rs3.imgui.ImGui;
import net.botwithus.rs3.script.ScriptConsole;
import net.botwithus.rs3.script.ScriptGraphicsContext;

public class SkeletonScriptGraphicsContext extends ScriptGraphicsContext {
    private final SkeletonScript script;
    private long scriptStartTime;
    private int startingXP;
    boolean isScriptRunning = false;
    private final int startingFiremakingLevel;
    private static final int[] levelXP = new int[]{0, 83, 174, 276, 388, 512, 650, 801, 969, 1154, 1358, 1584, 1833, 2107, 2411, 2746, 3115, 3523, 3973, 4470, 5018, 5624, 6291, 7028, 7842, 8740, 9730, 10824, 12031, 13363, 14833, 16456, 18247, 20224, 22406, 24815, 27473, 30408, 33648, 37224, 41171, 45529, 50339, 55649, 61512, 67983, 75127, 83014, 91721, 101333, 111945, 123660, 136594, 150872, 166636, 184040, 203254, 224466, 247886, 273742, 302288, 333804, 368599, 407015, 449428, 496254, 547953, 605032, 668051, 737627, 814445, 899257, 992895, 1096278, 1210421, 1336443, 1475581, 1629200, 1798808, 1986068, 2192818, 2421087, 2673114, 2951373, 3258594, 3597792, 3972294, 4385776, 4842295, 5346332, 5902831, 6517253, 7195629, 7944614, 8771558, 9684577, 10692629, 11805606, 13034431, 14391160, 15889109, 17542976, 19368992, 21385073, 23611006, 26068632, 28782069, 31777943, 35085654, 38737661, 42769801, 47221641, 52136869, 57563718, 63555443, 70170840, 77474828, 85539082, 94442737, 104273167};

    public static int XPtoLVL(int xpIN) {
        int lvlCalc = 0;
        if (xpIN >= levelXP[119]) {
            lvlCalc = 120;
        } else {
            for(int i = 0; xpIN >= levelXP[i]; ++i) {
                lvlCalc = i + 1;
            }
        }

        return lvlCalc;
    }

    public SkeletonScriptGraphicsContext(ScriptConsole console, SkeletonScript script) {
        super(console);
        this.script = script;
        this.startingXP = Skills.FIREMAKING.getSkill().getExperience();
        this.scriptStartTime = System.currentTimeMillis();
        this.startingFiremakingLevel = script.getStartingFiremakingLevel();
    }

    public void drawSettings() {
        ImGui.PushStyleColor(21, RGBToFloat(47), RGBToFloat(79), RGBToFloat(79), 1.0F);
        ImGui.PushStyleColor(18, RGBToFloat(255), RGBToFloat(255), RGBToFloat(255), 1.0F);
        ImGui.PushStyleColor(5, RGBToFloat(47), RGBToFloat(79), RGBToFloat(79), 1.0F);
        ImGui.PushStyleColor(2, RGBToFloat(0), RGBToFloat(0), RGBToFloat(0), 0.9F);
        ImGui.PushStyleColor(7, RGBToFloat(47), RGBToFloat(79), RGBToFloat(79), 1.0F);
        ImGui.PushStyleColor(11, RGBToFloat(47), RGBToFloat(79), RGBToFloat(79), 1.0F);
        ImGui.PushStyleColor(22, RGBToFloat(64), RGBToFloat(67), RGBToFloat(67), 1.0F);
        ImGui.PushStyleColor(27, RGBToFloat(47), RGBToFloat(79), RGBToFloat(79), 1.0F);
        ImGui.PushStyleColor(30, RGBToFloat(47), RGBToFloat(79), RGBToFloat(79), 1.0F);
        ImGui.SetWindowSize(600.0F, 600.0F);
        if (ImGui.Begin("Snows Accidental F&F", 0)) {
            ImGui.PushStyleVar(11, 50.0F, 5.0F);
            if (this.isScriptRunning) {
                if (ImGui.Button("Stop Script")) {
                    this.script.stopScript();
                    this.isScriptRunning = false;
                }
            } else if (ImGui.Button("Start Script")) {
                this.script.startScript();
                this.isScriptRunning = true;
            }

            ImGui.PopStyleVar(3);
            ImGui.Separator();
            boolean currentDinosaurPropellantOption = this.script.isDinosaurPropellentOption();
            if (ImGui.Checkbox("Seggregation", currentDinosaurPropellantOption)) {
                this.script.setDinosaurPropellentOption(true);
                this.script.setDinoArrowsOption(false);
                this.script.setSharpShellsOption(false);
            }

            boolean currentSharpShellsOption = this.script.isSharpShellsOption();
            if (ImGui.Checkbox("Eggsperimentation", currentSharpShellsOption)) {
                this.script.setDinosaurPropellentOption(false);
                this.script.setDinoArrowsOption(false);
                this.script.setSharpShellsOption(true);
            }

            boolean currentDinoarrowsOption = this.script.isDinoArrowsOption();
            if (ImGui.Checkbox("Zygomite Carestyling", currentDinoarrowsOption)) {
                this.script.setDinosaurPropellentOption(false);
                this.script.setDinoArrowsOption(true);
                this.script.setSharpShellsOption(false);
            }

            if (this.script.isDinoArrowsOption()) {
                ImGui.SameLine();
                boolean currentCutOption = this.script.isCutOption();
                if (ImGui.Checkbox("Cut", currentCutOption)) {
                    this.script.setCutOption(true);
                    this.script.setDryOption(false);
                    this.script.setCutAndDryOption(false);
                }

                ImGui.SameLine();
                boolean currentDryOption = this.script.isDryOption();
                if (ImGui.Checkbox("Dry", currentDryOption)) {
                    this.script.setCutOption(false);
                    this.script.setDryOption(true);
                    this.script.setCutAndDryOption(false);
                }

                ImGui.SameLine();
                boolean currentCutAndDryOption = this.script.isCutAndDryOption();
                if (ImGui.Checkbox("Cut&Dry", currentCutAndDryOption)) {
                    this.script.setCutOption(false);
                    this.script.setDryOption(false);
                    this.script.setCutAndDryOption(true);
                }
            }

            long elapsedTimeMillis = System.currentTimeMillis() - this.scriptStartTime;
            long elapsedSeconds = elapsedTimeMillis / 1000L;
            long hours = elapsedSeconds / 3600L;
            long minutes = elapsedSeconds % 3600L / 60L;
            long seconds = elapsedSeconds % 60L;
            String displayTimeRunning = String.format("%02d:%02d:%02d", hours, minutes, seconds);
            ImGui.SeparatorText("Time Running  " + displayTimeRunning);
            int currentLevel = XPtoLVL(Skills.FIREMAKING.getSkill().getExperience());
            int levelsGained = currentLevel - this.startingFiremakingLevel;
            ImGui.Text("Current Firemaking Level: " + currentLevel + "  (" + levelsGained + " Gained)", new Object[0]);
            int currentXP = Skills.FIREMAKING.getSkill().getExperience();
            currentLevel = Skills.FIREMAKING.getSkill().getLevel();
            int xpForNextLevel = Skills.FIREMAKING.getExperienceAt(currentLevel + 1);
            int xpTillNextLevel = xpForNextLevel - currentXP;
            ImGui.Text("XP remaining: " + xpTillNextLevel, new Object[0]);
            this.displayXPGained(Skills.FIREMAKING);
            this.displayXpPerHour(Skills.FIREMAKING);
            String timeToLevelStr = this.calculateTimeTillNextLevel();
            ImGui.Text(timeToLevelStr, new Object[0]);
            ImGui.Separator();
            this.displayXpProgressBar();
            ImGui.Separator();
            ImGui.PopStyleColor(100);
            ImGui.PushStyleColor(0, RGBToFloat(255), RGBToFloat(255), RGBToFloat(255), 0.7F);
            ImGui.Text("Script Information:", new Object[0]);
            ImGui.Text("Start in the required areas", new Object[0]);
            ImGui.Text("Make sure to have Potterington Blend #102 on action bar", new Object[0]);
            ImGui.Text("Must have required Levels/Extinction quest", new Object[0]);
            ImGui.End();
        }

        ImGui.PopStyleColor(1);
    }

    private void displayXPGained(Skills skill) {
        int currentXP = skill.getSkill().getExperience();
        int xpGained = currentXP - this.startingXP;
        ImGui.Text("XP Gained: " + xpGained, new Object[0]);
    }

    private void displayXpPerHour(Skills skill) {
        long elapsedTime = System.currentTimeMillis() - this.scriptStartTime;
        double hoursElapsed = (double)elapsedTime / 3600000.0;
        int currentXP = skill.getSkill().getExperience();
        int xpGained = currentXP - this.startingXP;
        double xpPerHour = hoursElapsed > 0.0 ? (double)xpGained / hoursElapsed : 0.0;
        String formattedXpPerHour = this.formatNumberForDisplay(xpPerHour);
        ImGui.Text("XP Per Hour: " + formattedXpPerHour, new Object[0]);
    }

    private String formatNumberForDisplay(double number) {
        if (number < 1000.0) {
            return String.format("%.0f", number);
        } else if (number < 1000000.0) {
            return String.format("%.1fk", number / 1000.0);
        } else {
            return number < 1.0E9 ? String.format("%.1fM", number / 1000000.0) : String.format("%.1fB", number / 1.0E9);
        }
    }

    private static float RGBToFloat(int rgbValue) {
        return (float)rgbValue / 255.0F;
    }

    private void displayXpProgressBar() {
        int currentXP = Skills.FIREMAKING.getSkill().getExperience();
        int currentLevel = Skills.FIREMAKING.getSkill().getLevel();
        int xpForNextLevel = Skills.FIREMAKING.getExperienceAt(currentLevel + 1);
        int xpForCurrentLevel = Skills.FIREMAKING.getExperienceAt(currentLevel);
        int xpToNextLevel = xpForNextLevel - xpForCurrentLevel;
        int xpGainedTowardsNextLevel = currentXP - xpForCurrentLevel;
        float progress = (float)xpGainedTowardsNextLevel / (float)xpToNextLevel;
        float[][] colors = new float[][]{{1.0F, 0.0F, 0.0F, 1.0F}, {1.0F, 0.4F, 0.4F, 1.0F}, {1.0F, 0.6F, 0.0F, 1.0F}, {1.0F, 0.7F, 0.4F, 1.0F}, {1.0F, 1.0F, 0.0F, 1.0F}, {0.8F, 1.0F, 0.4F, 1.0F}, {0.6F, 1.0F, 0.6F, 1.0F}, {0.4F, 1.0F, 0.4F, 1.0F}, {0.3F, 0.9F, 0.3F, 1.0F}, {0.2F, 0.8F, 0.2F, 1.0F}, {0.1F, 0.7F, 0.1F, 1.0F}};
        int index = (int)(progress * 10.0F);
        float blend = progress * 10.0F - (float)index;
        if (index >= colors.length - 1) {
            index = colors.length - 2;
            blend = 1.0F;
        }

        float[] startColor = colors[index];
        float[] endColor = colors[index + 1];
        float[] currentColor = new float[]{startColor[0] + blend * (endColor[0] - startColor[0]), startColor[1] + blend * (endColor[1] - startColor[1]), startColor[2] + blend * (endColor[2] - startColor[2]), 1.0F};
        ImGui.PushStyleColor(42, currentColor[0], currentColor[1], currentColor[2], currentColor[3]);
        ImGui.Text("XP Progress to Next Level:", new Object[0]);
        ImGui.PushStyleColor(0, RGBToFloat(0), RGBToFloat(0), RGBToFloat(0), 0.0F);
        ImGui.ProgressBar(String.format("%.2f%%", progress * 100.0F), progress, 200.0F, 15.0F);
        ImGui.PopStyleColor(2);
    }

    private String calculateTimeTillNextLevel() {
        int currentXP = Skills.FIREMAKING.getSkill().getExperience();
        int currentLevel = Skills.FIREMAKING.getSkill().getLevel();
        int xpForNextLevel = Skills.FIREMAKING.getExperienceAt(currentLevel + 1);
        int xpForCurrentLevel = Skills.FIREMAKING.getExperienceAt(currentLevel);
        int var10000 = currentXP - xpForCurrentLevel;
        long currentTime = System.currentTimeMillis();
        int xpGained = currentXP - this.startingXP;
        long timeElapsed = currentTime - this.scriptStartTime;
        if (xpGained > 0 && timeElapsed > 0L) {
            double xpPerMillisecond = (double)xpGained / (double)timeElapsed;
            long timeToLevelMillis = (long)((double)(xpForNextLevel - currentXP) / xpPerMillisecond);
            long timeToLevelSecs = timeToLevelMillis / 1000L;
            long hours = timeToLevelSecs / 3600L;
            long minutes = timeToLevelSecs % 3600L / 60L;
            long seconds = timeToLevelSecs % 60L;
            return String.format("Time to level: %02d:%02d:%02d", hours, minutes, seconds);
        } else {
            return "Time to level: calculating...";
        }
    }
}
