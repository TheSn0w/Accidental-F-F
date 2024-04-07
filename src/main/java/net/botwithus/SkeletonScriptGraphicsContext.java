package net.botwithus;

import net.botwithus.rs3.game.skills.Skills;
import net.botwithus.rs3.imgui.ImGui;
import net.botwithus.rs3.imgui.ImGuiWindowFlag;
import net.botwithus.rs3.script.ScriptConsole;
import net.botwithus.rs3.script.ScriptGraphicsContext;

import java.time.Duration;
import java.time.Instant;

public class SkeletonScriptGraphicsContext extends ScriptGraphicsContext {

    private SkeletonScript script;
    boolean isScriptRunning = false;
    private long totalElapsedTime = 0;
    private Instant startTime;
    private int startingXP;
    private long scriptStartTime;
    int startingFiremakingXP = Skills.FIREMAKING.getSkill().getExperience();
    private final int startingFiremakingLevel;

    public SkeletonScriptGraphicsContext(ScriptConsole scriptConsole, SkeletonScript script) {
        super(scriptConsole);
        this.script = script;
        startTime = Instant.now();
        startingXP = Skills.FIREMAKING.getSkill().getExperience();
        scriptStartTime = System.currentTimeMillis();
        this.startingFiremakingLevel = script.getStartingFiremakingLevel();
    }
    private static float RGBToFloat(int rgbValue) {
        return rgbValue / 255.0f;
    }

    @Override
    public void drawSettings() {
        ImGui.PushStyleColor(0, RGBToFloat(173), RGBToFloat(216), RGBToFloat(230), 0.8f); // Button color
        ImGui.PushStyleColor(21, RGBToFloat(47), RGBToFloat(79), RGBToFloat(79), 0.5f); // Button color
        ImGui.PushStyleColor(18, RGBToFloat(173), RGBToFloat(216), RGBToFloat(230), 1.0f); // Checkbox Tick color
        ImGui.PushStyleColor(5, RGBToFloat(47), RGBToFloat(79), RGBToFloat(79), 0.5f); // Border Colour
        ImGui.PushStyleColor(2, RGBToFloat(0), RGBToFloat(0), RGBToFloat(0), 0.9f); // Background color
        ImGui.PushStyleColor(7, RGBToFloat(47), RGBToFloat(79), RGBToFloat(79), 0.5f); // Checkbox Background color
        ImGui.PushStyleColor(11, RGBToFloat(47), RGBToFloat(79), RGBToFloat(79), 0.5f); // Header Colour
        ImGui.PushStyleColor(22, RGBToFloat(47), RGBToFloat(79), RGBToFloat(79), 0.8f); // Highlighted button color
        ImGui.PushStyleColor(13, RGBToFloat(47), RGBToFloat(79), RGBToFloat(79), 0.5f); // Highlighted button color
        ImGui.PushStyleColor(27, RGBToFloat(47), RGBToFloat(79), RGBToFloat(79), 0.5f); //ImGUI separator Colour
        ImGui.PushStyleColor(30, RGBToFloat(47), RGBToFloat(79), RGBToFloat(79), 0.5f); //Corner Extender colour
        ImGui.PushStyleColor(31, RGBToFloat(47), RGBToFloat(79), RGBToFloat(79), 0.5f); //Corner Extender colour
        ImGui.PushStyleColor(32, RGBToFloat(47), RGBToFloat(79), RGBToFloat(79), 0.5f); //Corner Extender colour
        ImGui.PushStyleColor(33, RGBToFloat(47), RGBToFloat(79), RGBToFloat(79), 0.5f); //Corner Extender colour
        ImGui.PushStyleColor(3, RGBToFloat(47), RGBToFloat(79), RGBToFloat(79), 0.5f); //ChildBackground


        ImGui.SetWindowSize(200.f, 200.f);
        if (ImGui.Begin("Snows F&F", ImGuiWindowFlag.None.getValue())) {
            ImGui.PushStyleVar(1, 10.f, 5f);
            ImGui.PushStyleVar(2, 10.f, 5f); //spacing between side of window and checkbox
            ImGui.PushStyleVar(3, 10.f, 5f);
            ImGui.PushStyleVar(4, 10.f, 10f);
            ImGui.PushStyleVar(5, 10.f, 5f);
            ImGui.PushStyleVar(6, 10.f, 5f);
            ImGui.PushStyleVar(7, 10.f, 5f);
            ImGui.PushStyleVar(8, 10.f, 5f); //spacing between seperator and text
            ImGui.PushStyleVar(9, 10.f, 5f);
            ImGui.PushStyleVar(10, 10.f, 5f);
            ImGui.PushStyleVar(11, 10.f, 5f); // button sizes
            ImGui.PushStyleVar(12, 10.f, 5f);
            ImGui.PushStyleVar(13, 10.f, 5f);
            ImGui.PushStyleVar(14, 10.f, 5f); // spaces between options ontop such as overlays, debug etc
            ImGui.PushStyleVar(15, 10.f, 5f); // spacing between Text/tabs and checkboxes
            ImGui.PushStyleVar(16, 10.f, 5f);
            ImGui.PushStyleVar(17, 10.f, 5f);
            if (isScriptRunning) {
                if (ImGui.Button("Stop Script")) {
                    script.stopScript();
                    totalElapsedTime += Duration.between(startTime, Instant.now()).getSeconds();
                    isScriptRunning = false;
                }
            } else {
                if (ImGui.Button("Start Script")) {
                    script.startScript();
                    startTime = Instant.now();
                    isScriptRunning = true;
                }
            }

            long elapsedTime = isScriptRunning ? Duration.between(startTime, Instant.now()).getSeconds() + totalElapsedTime : totalElapsedTime;
            ImGui.SeparatorText(String.format("Runtime: %02d:%02d:%02d", elapsedTime / 3600, (elapsedTime % 3600) / 60, elapsedTime % 60));
            script.CollectEggs = ImGui.Checkbox("Collect eggs", script.CollectEggs);
            script.CollectFertiliser = ImGui.Checkbox("Collect fertiliser", script.CollectFertiliser);
            script.Zygomite = ImGui.Checkbox("Zygomite", script.Zygomite);
            if (script.Zygomite) {

                boolean wasCutSelected = script.cutOption;
                if (ImGui.Checkbox("Cut", script.cutOption)) {
                    script.cutOption = true;
                    if (wasCutSelected != script.cutOption) {
                        script.dryOption = false;
                        script.cutAndDryOption = false;
                    }
                }
                ImGui.SameLine();

                boolean wasDrySelected = script.dryOption;
                if (ImGui.Checkbox("Dry", script.dryOption)) {
                    script.dryOption = true;
                    if (wasDrySelected != script.dryOption) {
                        script.cutOption = false;
                        script.cutAndDryOption = false;
                    }
                }
                ImGui.SameLine();

                boolean wasCutAndDrySelected = script.cutAndDryOption;
                if (ImGui.Checkbox("Cut and Dry", script.cutAndDryOption)) {
                    script.cutAndDryOption = true;
                    if (wasCutAndDrySelected != script.cutAndDryOption) {
                        script.cutOption = false;
                        script.dryOption = false;
                    }
                }
            }
            long elapsedTimeMillis = System.currentTimeMillis() - this.scriptStartTime;
            long elapsedSeconds = elapsedTimeMillis / 1000L;
            long hours = elapsedSeconds / 3600L;
            long minutes = elapsedSeconds % 3600L / 60L;
            long seconds = elapsedSeconds % 60L;
            String displayTimeRunning = String.format("%02d:%02d:%02d", hours, minutes, seconds);
            ImGui.SeparatorText("Time Running  " + displayTimeRunning);
            displayStatsForSkill(Skills.FIREMAKING, "Firemaking", startingFiremakingLevel, startingFiremakingXP);
            ImGui.EndTabItem();

            ImGui.EndTabBar();
        }
        ImGui.End();
        ImGui.PopStyleVar(20);
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
    private void displayStatsForSkill(Skills skill, String skillName, int startingLevel, int startingXP) {
        int currentLevel = skill.getSkill().getLevel();
        int levelsGained = currentLevel - startingLevel;
        /*ImGui.Text("Current " + skillName + " Level: " + currentLevel + "  (" + levelsGained + " Gained)");*/

        int currentXP = skill.getSkill().getExperience();
        int xpForNextLevel = skill.getExperienceAt(currentLevel + 1);
        int xpTillNextLevel = xpForNextLevel - currentXP;
        ImGui.Text(skillName + " XP remaining: " + xpTillNextLevel);

        displayXPGained(skill, startingXP);
        displayXpPerHour(skill, startingXP);
        String timeToLevelStr = calculateTimeTillNextLevel(skill, startingXP);
        ImGui.Text(timeToLevelStr);
    }

    private void displayXPGained(Skills skill, int startingXP) {
        int currentXP = skill.getSkill().getExperience();
        int xpGained = currentXP - startingXP;
        ImGui.Text("XP Gained: " + xpGained);
    }

    private void displayXpPerHour(Skills skill, int startingXP) {
        long elapsedTime = System.currentTimeMillis() - scriptStartTime;
        double hoursElapsed = elapsedTime / (1000.0 * 60 * 60);
        int currentXP = skill.getSkill().getExperience();
        int xpGained = currentXP - startingXP;
        double xpPerHour = hoursElapsed > 0 ? xpGained / hoursElapsed : 0;
        String formattedXpPerHour = formatNumberForDisplay(xpPerHour);
        ImGui.Text("XP Per Hour: " + formattedXpPerHour);
    }

    private String calculateTimeTillNextLevel(Skills skill, int startingXP) {
        int currentXP = skill.getSkill().getExperience();
        int currentLevel = skill.getSkill().getLevel();
        int xpForNextLevel = skill.getExperienceAt(currentLevel + 1);
        int xpGained = currentXP - startingXP;
        long timeElapsed = System.currentTimeMillis() - scriptStartTime;

        if (xpGained > 0 && timeElapsed > 0) {
            double xpPerMillisecond = xpGained / (double) timeElapsed;
            long timeToLevelMillis = (long) ((xpForNextLevel - currentXP) / xpPerMillisecond);
            long timeToLevelSecs = timeToLevelMillis / 1000;
            long hours = timeToLevelSecs / 3600;
            long minutes = (timeToLevelSecs % 3600) / 60;
            long seconds = timeToLevelSecs % 60;

            return String.format("Time remaining to next level: " + "%02d:%02d:%02d", hours, minutes, seconds);
        } else {
            return "Calculating time to next level...";
        }
    }

    @Override
    public void drawOverlay() {
        super.drawOverlay();
    }
}
