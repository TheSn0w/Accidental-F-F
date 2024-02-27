package net.botwithus;

import java.time.Instant;
import java.util.Iterator;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicBoolean;
import net.botwithus.api.game.hud.inventories.Backpack;
import net.botwithus.internal.scripts.ScriptDefinition;
import net.botwithus.rs3.game.Client;
import net.botwithus.rs3.game.Coordinate;
import net.botwithus.rs3.game.Distance;
import net.botwithus.rs3.game.Item;
import net.botwithus.rs3.game.Travel;
import net.botwithus.rs3.game.actionbar.ActionBar;
import net.botwithus.rs3.game.hud.interfaces.Component;
import net.botwithus.rs3.game.hud.interfaces.Interfaces;
import net.botwithus.rs3.game.minimenu.MiniMenu;
import net.botwithus.rs3.game.minimenu.actions.ComponentAction;
import net.botwithus.rs3.game.queries.builders.characters.NpcQuery;
import net.botwithus.rs3.game.queries.builders.components.ComponentQuery;
import net.botwithus.rs3.game.queries.builders.items.InventoryItemQuery;
import net.botwithus.rs3.game.queries.builders.objects.SceneObjectQuery;
import net.botwithus.rs3.game.queries.results.EntityResultSet;
import net.botwithus.rs3.game.queries.results.ResultSet;
import net.botwithus.rs3.game.scene.entities.characters.npc.Npc;
import net.botwithus.rs3.game.scene.entities.characters.player.LocalPlayer;
import net.botwithus.rs3.game.scene.entities.characters.player.Player;
import net.botwithus.rs3.game.scene.entities.object.SceneObject;
import net.botwithus.rs3.game.skills.Skills;
import net.botwithus.rs3.script.Execution;
import net.botwithus.rs3.script.LoopingScript;
import net.botwithus.rs3.script.config.ScriptConfig;
import net.botwithus.rs3.util.RandomGenerator;

public class SkeletonScript extends LoopingScript {
    private ScriptState currentState;
    private int startingFiremakingLevel;
    public boolean runScript = false;
    private boolean scriptRunning = false;
    private Instant scriptStartTime;
    private int startingXP;
    private boolean dinosaurPropellent = false;
    private boolean sharpShells = false;
    private boolean dinoArrows = false;
    private boolean lastHandledWereBadEggs = false;
    private boolean atBank = false;
    private HandleSharpShells currentSharpShellsState;
    boolean cutOption;
    boolean dryOption;
    boolean cutAndDryOption;
    private HandleDinoArrows currentDinoState;

    public SkeletonScript(String name, ScriptConfig scriptConfig, ScriptDefinition scriptDefinition) {
        super(name, scriptConfig, scriptDefinition);
        this.currentSharpShellsState = SkeletonScript.HandleSharpShells.COLLECT_EGGS;
        this.cutOption = this.isCutOption();
        this.dryOption = this.isDryOption();
        this.cutAndDryOption = this.isCutAndDryOption();
        this.currentDinoState = SkeletonScript.HandleDinoArrows.CONFIGURE;
        this.currentState = SkeletonScript.ScriptState.COLLECT_FROM_STORM_BARN;
    }

    public void startScript() {
        if (!this.scriptRunning) {
            this.scriptRunning = true;
            this.scriptStartTime = Instant.now();
            this.currentState = SkeletonScript.ScriptState.COLLECT_FROM_STORM_BARN;
            this.currentSharpShellsState = SkeletonScript.HandleSharpShells.COLLECT_EGGS;
            this.println("Script started successfully.");
        }

    }

    public void stopScript() {
        if (this.scriptRunning) {
            this.scriptRunning = false;
            this.println("Script stopped successfully.");
            this.currentState = SkeletonScript.ScriptState.COLLECT_FROM_STORM_BARN;
            this.currentSharpShellsState = SkeletonScript.HandleSharpShells.COLLECT_EGGS;
        }

    }

    public boolean initialize() {
        this.startingFiremakingLevel = Skills.FIREMAKING.getSkill().getLevel();
        this.sgc = new SkeletonScriptGraphicsContext(this.getConsole(), this);
        this.loopDelay = 590L;
        return super.initialize();
    }

    public void onLoop() {
        if (this.scriptRunning) {
            if (this.isDinosaurPropellentOption()) {
                this.HandleDinosaurPropellent();
            } else if (this.isSharpShellsOption()) {
                this.handleSharpShells();
            } else if (this.isDinoArrowsOption()) {
                this.HandleDinoArrows();
            }
        }

    }

    public void HandleDinosaurPropellent() {
        if (this.scriptRunning) {
            switch (this.currentState) {
                case COLLECT_FROM_STORM_BARN:
                    this.collectFromStormBarn();
                    if (Backpack.isFull()) {
                        this.currentState = SkeletonScript.ScriptState.FEED_TUMMISAURUS;
                    }
                    break;
                case FEED_TUMMISAURUS:
                    this.customDelay();
                    this.feedAtTrough("Rooty mush trough", "Tummisaurus Rex", 5289, 2259, 0, SkeletonScript.ScriptState.MOVE_TO_INTERMEDIATE_POINT);
                    this.currentState = SkeletonScript.ScriptState.MOVE_TO_INTERMEDIATE_POINT;
                    break;
                case MOVE_TO_INTERMEDIATE_POINT:
                    this.customDelay();
                    this.moveToIntermediatePoint();
                    this.currentState = SkeletonScript.ScriptState.FEED_ROOTLESAURUS;
                    break;
                case FEED_ROOTLESAURUS:
                    this.customDelay();
                    this.feedAtTrough("Beany mush trough", "Rootlesaurus Rex", 5313, 2307, 0, SkeletonScript.ScriptState.FEED_BEANASAURUS);
                    this.currentState = SkeletonScript.ScriptState.FEED_BEANASAURUS;
                    break;
                case FEED_BEANASAURUS:
                    this.customDelay();
                    this.feedAtTrough("Berry mush trough", "Beanasaurus Rex", 5332, 2291, 0, SkeletonScript.ScriptState.FEED_BERRISAURUS);
                    this.currentState = SkeletonScript.ScriptState.FEED_BERRISAURUS;
                    break;
                case FEED_BERRISAURUS:
                    this.customDelay();
                    this.feedAtTrough("Cerealy mush trough", "Berrisaurus Rex", 5330, 2271, 0, SkeletonScript.ScriptState.USE_FERTILISER);
                    this.currentState = SkeletonScript.ScriptState.USE_FERTILISER;
                    break;
                case USE_FERTILISER:
                    this.customDelay();
                    this.useFertiliser();
                    this.currentState = SkeletonScript.ScriptState.COLLECT_FROM_STORM_BARN;
            }
        }

    }

    public boolean isDinosaurPropellentOption() {
        return this.dinosaurPropellent;
    }

    public void setDinosaurPropellentOption(boolean dinosaurPropellent) {
        this.dinosaurPropellent = dinosaurPropellent;
        if (dinosaurPropellent) {
            this.sharpShells = false;
            this.dinoArrows = false;
        }

    }

    public boolean isSharpShellsOption() {
        return this.sharpShells;
    }

    public void setSharpShellsOption(boolean sharpShells) {
        this.sharpShells = sharpShells;
        if (sharpShells) {
            this.dinosaurPropellent = false;
            this.dinoArrows = false;
        }

    }

    public boolean isDinoArrowsOption() {
        return this.dinoArrows;
    }

    public void setDinoArrowsOption(boolean dinoArrows) {
        this.dinoArrows = dinoArrows;
        if (dinoArrows) {
            this.dinosaurPropellent = false;
            this.sharpShells = false;
        }

    }

    public int getStartingFiremakingLevel() {
        return this.startingFiremakingLevel;
    }

    private void moveToIntermediatePoint() {
        boolean skipFirstTarget = RandomGenerator.nextBoolean();
        boolean reachedFirstTarget = !skipFirstTarget;
        boolean reachedSecondTarget = false;
        Coordinate secondTargetCoord;
        if (!skipFirstTarget) {
            secondTargetCoord = this.randomizedCoordinate(5298, 2272, 0);
            this.println("Attempting first target intermediate point: " + String.valueOf(secondTargetCoord));
            this.moveAndWait(secondTargetCoord);
        } else {
            this.println("Skipping first intermediate point.");
        }

        secondTargetCoord = this.randomizedCoordinate(5310, 2290, 0);
        this.println("Attempting second target intermediate point: " + String.valueOf(secondTargetCoord));
        reachedSecondTarget = this.moveAndWait(secondTargetCoord);
        if (reachedSecondTarget) {
            this.currentState = SkeletonScript.ScriptState.FEED_ROOTLESAURUS;
        } else {
            this.println("Failed to reach the second intermediate point.");
        }

    }

    private boolean moveAndWait(Coordinate targetCoord) {
        Travel.walkTo(targetCoord);
        long startTime = System.currentTimeMillis();
        long timeout = 60000L;

        while(System.currentTimeMillis() - startTime < timeout) {
            if (Client.getLocalPlayer() == null) {
                this.println("Local player instance not found.");
                return false;
            }

            Coordinate currentLocation = Client.getLocalPlayer().getCoordinate();
            double distance = Distance.between(currentLocation, targetCoord);
            if (distance <= 8.0) {
                this.println("Reached intermediate point: " + String.valueOf(targetCoord));
                return true;
            }
        }

        this.println("Timeout reached while trying to move to: " + String.valueOf(targetCoord));
        return false;
    }

    private void collectFromStormBarn() {
        if (Backpack.isFull()) {
            this.println("Backpack is full, skipping collection from Storm Barn.");
        } else {
            SceneObject stormBarn = (SceneObject)SceneObjectQuery.newQuery().name("Storm barn").results().nearestTo(this.randomizedCoordinate(5304, 2276, 0));
            if (stormBarn != null) {
                this.println("Collecting from Storm Barn.");
                stormBarn.interact("Collect");
                Execution.delayUntil(30000L, Backpack::isFull);
                this.currentState = ScriptState.FEED_TUMMISAURUS;
            }

        }
    }

    private void feedAtTrough(String troughName, String dinoName, int x, int y, int z, ScriptState feedingState) {
        while(Backpack.contains(new String[]{dinoName})) {
            SceneObject trough = (SceneObject)SceneObjectQuery.newQuery().name(troughName).results().nearestTo(this.randomizedCoordinate(x, y, z));
            if (trough != null) {
                this.println("Moving to " + troughName + " to feed " + dinoName + ".");
                this.println("Feeding " + dinoName + " at " + troughName + ".");
                trough.interact("Feed");
                Execution.delayUntil(60000L, () -> {
                    return !Backpack.contains(new String[]{dinoName});
                });
                this.randomDelay();
            }
        }

    }

    private Coordinate randomizedCoordinate(int x, int y, int z) {
        int randomX = x + ThreadLocalRandom.current().nextInt(-3, 2);
        int randomY = y + ThreadLocalRandom.current().nextInt(-3, 2);
        Coordinate coord = new Coordinate(randomX, randomY, z);
        this.println("Generated randomized coordinate: " + String.valueOf(coord));
        return coord;
    }

    private void useFertiliser() {
        while(Backpack.contains(new String[]{"Potterington Blend #102 Fertiliser"})) {
            this.println("Using Potterington Blend #102 Fertiliser");
            ActionBar.useItem("Potterington Blend #102 Fertiliser", "Ignite");
            this.randomDelay();
            Execution.delayUntil(60000L, () -> {
                return !Backpack.contains(new String[]{"Potterington Blend #102 Fertiliser"});
            });
            this.randomDelay();
            this.currentState = SkeletonScript.ScriptState.COLLECT_FROM_STORM_BARN;
        }

    }

    private void randomDelay() {
        int delay = RandomGenerator.nextInt(250, 2000);
        this.println("Applying a random delay of " + delay + " ms.");
        Execution.delay((long)delay);
    }

    public void handleSharpShells() {
        switch (this.currentSharpShellsState) {
            case COLLECT_EGGS:
                if (Backpack.isFull()) {
                    if (this.hasDinosaursToDeposit()) {
                        this.currentSharpShellsState = SkeletonScript.HandleSharpShells.BANK_DINOSAURS;
                    } else {
                        this.currentSharpShellsState = SkeletonScript.HandleSharpShells.HANDLE_EGGS;
                    }
                } else {
                    this.collectEggs();
                }
                break;
            case HANDLE_EGGS:
                this.handleEggs();
                break;
            case BANK_DINOSAURS:
                this.bankDinosaurs();
                break;
            default:
                this.println("Script is idle or performing an unrecognized activity.");
        }

    }

    private void handleEggs() {
        if (!this.lastHandledWereBadEggs && this.hasBadEggs()) {
            this.customDelay();
            this.println("Handling Bad Eggs.");
            this.moveTo(5230, 2327);
            this.compostEggs();
            this.lastHandledWereBadEggs = true;
        } else if (this.hasGoodEggs()) {
            this.customDelay();
            this.println("Handling Good Eggs.");
            this.moveTo(5209, 2351);
            this.incubateEggs();
            this.lastHandledWereBadEggs = false;
        }

        this.customDelay();
        this.println("Returning to collect more eggs.");
        this.moveTo(5220, 2348);
        Execution.delayUntil(10000L, () -> {
            return !LocalPlayer.LOCAL_PLAYER.isMoving();
        });
        this.currentSharpShellsState = SkeletonScript.HandleSharpShells.COLLECT_EGGS;
    }

    private void moveTo(int x, int y) {
        int randomX = x + RandomGenerator.nextInt(-3, 4);
        int randomY = y + RandomGenerator.nextInt(-3, 4);
        this.println("Moving to coordinates: [" + randomX + ", " + randomY + "]");
        Travel.walkTo(new Coordinate(randomX, randomY, 0));
        this.println("Waiting for movement to complete...");
        this.waitForMovement();
    }

    private void collectEggs() {
        this.println("Collecting eggs from pile.");
        this.interactWithObject("Pile of dinosaur eggs", "Collect");
        Execution.delayUntil(60000L, Backpack::isFull);
        this.println("Checking if Backpack is full...");
    }

    private void compostEggs() {
        this.println("Composting Bad Eggs.");
        this.interactWithObject("Fast compost bin", "Compost");
        this.println("Waiting for no bad eggs in inventory...");
        this.waitForNoBadEggs();
    }

    private void incubateEggs() {
        this.println("Incubating Good Eggs.");
        this.interactWithObject("Fast incubator", "Incubate");
        this.println("Waiting for no good eggs in inventory...");
        this.waitForNoGoodEggs();
    }

    private void interactWithObject(String objectName, String action) {
        this.println("Interacting with: " + objectName);
        SceneObjectQuery query = SceneObjectQuery.newQuery().name(objectName).option(action);
        ResultSet<SceneObject> results = query.results();
        if (!results.isEmpty()) {
            SceneObject object = (SceneObject)results.first();

            assert object != null;

            object.interact(action);
        }

    }

    private boolean hasBadEggs() {
        InventoryItemQuery query = (InventoryItemQuery)InventoryItemQuery.newQuery(new int[0]).name("Bad egg");
        ResultSet<Item> results = query.results();
        return !results.isEmpty();
    }

    private boolean hasGoodEggs() {
        InventoryItemQuery query = (InventoryItemQuery)InventoryItemQuery.newQuery(new int[0]).name("Good egg");
        ResultSet<Item> results = query.results();
        return !results.isEmpty();
    }

    private void waitForMovement() {
        Player localPlayer = Client.getLocalPlayer();
        Execution.delayUntil(15000L, () -> {
            assert localPlayer != null;

            return !localPlayer.isMoving();
        });
    }

    private void waitForNoBadEggs() {
        Execution.delayUntil(60000L, () -> {
            return !this.hasBadEggs();
        });
        this.customDelay();
    }

    private void waitForNoGoodEggs() {
        Execution.delayUntil(60000L, () -> {
            return !this.hasGoodEggs();
        });
        this.customDelay();
    }

    private void bankDinosaurs() {
        this.println("Checking if at the bank...");
        if (!this.atBank) {
            this.println("Moving to the bank...");
            this.moveToBank();
        } else {
            this.println("At the bank, checking for dinosaurs to deposit...");
            if (this.hasDinosaursToDeposit()) {
                this.println("Depositing dinosaurs...");
                this.depositDinosaurs();
            } else {
                this.println("No dinosaurs to deposit.");
                this.currentSharpShellsState = SkeletonScript.HandleSharpShells.COLLECT_EGGS;
            }
        }

    }

    private boolean hasDinosaursToDeposit() {
        String[] dinosaurNames = new String[]{"Feral dinosaur (unchecked)", "Venomous dinosaur (unchecked)", "Ripper dinosaur (unchecked)", "Brutish dinosaur (unchecked)", "Arcane apoterrasaur (unchecked)", "Scimitops (unchecked)", "Bagrada rex (unchecked)", "Spicati apoterrasaur (unchecked)", "Asciatops (unchecked)", "Corbicula rex (unchecked)", "Oculi apoterrasaur (unchecked)", "Malletops (unchecked)", "Pavosaurus rex (unchecked)"};
        String[] var2 = dinosaurNames;
        int var3 = dinosaurNames.length;

        for(int var4 = 0; var4 < var3; ++var4) {
            String dinosaur = var2[var4];
            if (this.hasDinosaur(dinosaur)) {
                return true;
            }
        }

        return false;
    }

    private void moveToBank() {
        this.println("Moving to the Bank coordinates.");
        this.moveTo(5202, 2366);
        Execution.delayUntil(60000L, () -> {
            assert Client.getLocalPlayer() != null;

            Coordinate currentCoord = Client.getLocalPlayer().getCoordinate();
            return currentCoord.equals(new Coordinate(5202, 2366, 0));
        });
        this.atBank = true;
    }

    private void depositDinosaurs() {
        this.println("Depositing Dinosaurs...");
        AtomicBoolean depositedAny = new AtomicBoolean(false);
        String[] dinosaurNames = new String[]{"Feral dinosaur (unchecked)", "Venomous dinosaur (unchecked)", "Ripper dinosaur (unchecked)", "Brutish dinosaur (unchecked)", "Arcane apoterrasaur (unchecked)", "Scimitops (unchecked)", "Bagrada rex (unchecked)", "Spicati apoterrasaur (unchecked)", "Asciatops (unchecked)", "Corbicula rex (unchecked)", "Oculi apoterrasaur (unchecked)", "Malletops (unchecked)", "Pavosaurus rex (unchecked)"};
        String[] var3 = dinosaurNames;
        int var4 = dinosaurNames.length;

        for(int var5 = 0; var5 < var4; ++var5) {
            String dinosaur = var3[var5];
            if (this.hasDinosaur(dinosaur)) {
                ComponentQuery.newQuery(new int[]{517}).componentIndex(new int[]{15}).itemName(dinosaur).results().forEach((component) -> {
                    String depositAction = this.findDepositAction(component);
                    if (depositAction != null) {
                        component.interact(depositAction);
                        Execution.delay((long)RandomGenerator.nextInt(200, 400));
                        depositedAny.set(true);
                        Execution.delay((long)RandomGenerator.nextInt(300, 500));
                    }

                });
            }
        }

        if (depositedAny.get()) {
            this.println("Dinosaurs have been deposited.");
        } else {
            this.println("No dinosaurs to deposit.");
        }

        this.println("Returning to collect more eggs.");
        this.collectEggs();
    }

    private String findDepositAction(Component component) {
        Iterator var2 = component.getOptions().iterator();

        String option;
        do {
            if (!var2.hasNext()) {
                return null;
            }

            option = (String)var2.next();
        } while(!option.startsWith("Deposit"));

        return option;
    }

    private boolean hasDinosaur(String dinosaurName) {
        InventoryItemQuery query = (InventoryItemQuery)InventoryItemQuery.newQuery(new int[0]).name(dinosaurName);
        return !query.results().isEmpty();
    }

    private void customDelay() {
        double chance = Math.random() * 100.0;
        int delay;
        if (chance < 5.0) {
            delay = RandomGenerator.nextInt(10000, 15001);
            this.println("Applying a long delay of " + delay + "ms (5% chance range)");
        } else if (chance < 15.0) {
            delay = RandomGenerator.nextInt(3000, 5001);
            this.println("Applying a moderate delay of " + delay + "ms (10% chance range)");
        } else {
            delay = RandomGenerator.nextInt(300, 1301);
            this.println("Applying a short delay of " + delay + "ms (default range)");
        }

        Execution.delay((long)delay);
    }

    public boolean isCutOption() {
        return this.cutOption;
    }

    public void setCutOption(boolean cutOption) {
        this.cutOption = cutOption;
    }

    public boolean isDryOption() {
        return this.dryOption;
    }

    public void setDryOption(boolean dryOption) {
        this.dryOption = dryOption;
    }

    public boolean isCutAndDryOption() {
        return this.cutAndDryOption;
    }

    public void setCutAndDryOption(boolean cutAndDryOption) {
        this.cutAndDryOption = cutAndDryOption;
    }

    public void HandleDinoArrows() {
        switch (this.currentDinoState) {
            case CONFIGURE:
                this.interactWithScruffy("Configure");
                if (Interfaces.isOpen(720)) {
                    this.currentDinoState = SkeletonScript.HandleDinoArrows.SELECT_OPTION;
                }
                break;
            case SELECT_OPTION:
                this.selectOptionBasedOnGUI();
                this.currentDinoState = SkeletonScript.HandleDinoArrows.PERFORM_ACTION;
                break;
            case PERFORM_ACTION:
                this.performActionBasedOnOption();
                this.currentDinoState = SkeletonScript.HandleDinoArrows.WAIT_FOR_ANIMATION_END;
                break;
            case WAIT_FOR_ANIMATION_END:
                this.interactWithAnyNpcFromList();
                this.currentDinoState = SkeletonScript.HandleDinoArrows.PERFORM_ACTION;
        }

        Execution.delay(1000L);
    }

    private void interactWithScruffy(String action) {
        EntityResultSet<Npc> npcs = ((NpcQuery)((NpcQuery)NpcQuery.newQuery().name("Scruffy zygomite")).option(action)).results();
        if (!npcs.isEmpty()) {
            assert Client.getLocalPlayer() != null;

            Npc scruffy = (Npc)npcs.nearestTo(Client.getLocalPlayer().getCoordinate());
            if (scruffy != null) {
                boolean interactionSuccess = scruffy.interact(action);
                if (interactionSuccess) {
                    this.println("Successfully interacted with Scruffy Zygomite.");
                } else {
                    this.println("Failed to interact with Scruffy Zygomite.");
                }
            }
        } else {
            this.println("Scruffy Zygomite not found.");
        }

        this.customDelay();
    }

    private void selectOptionBasedOnGUI() {
        String selectedOption = "";
        if (this.cutOption) {
            selectedOption = "Cut";
            MiniMenu.interact(ComponentAction.DIALOGUE.getType(), 0, -1, 47185940);
        } else if (this.dryOption) {
            selectedOption = "Dry";
            MiniMenu.interact(ComponentAction.DIALOGUE.getType(), 0, -1, 47185943);
        } else if (this.cutAndDryOption) {
            selectedOption = "Cut and Dry";
            MiniMenu.interact(ComponentAction.DIALOGUE.getType(), 0, -1, 47185921);
        }

        this.println("Option selected: " + selectedOption);
        Execution.delayUntil(5000L, () -> {
            return !Interfaces.isOpen(720);
        });
        Execution.delay((long)RandomGenerator.nextInt(1000, 2000));
        this.println("Small delay");
    }

    private void performActionBasedOnOption() {
        if (this.cutOption) {
            this.interactWithScruffy("Cut");
        } else if (this.dryOption) {
            this.interactWithScruffy("Dry");
        } else if (this.cutAndDryOption) {
            this.interactWithScruffy("Cut and dry");
        } else {
            this.println("No action selected for Scruffy zygomite.");
        }

    }

    private void interactWithAnyNpcFromList() {
        int[] npcIds = new int[]{31239, 31242, 31238, 31237, 31241, 31243};
        boolean npcInteracted = false;

        while(!npcInteracted) {
            int[] var3 = npcIds;
            int var4 = npcIds.length;

            for(int var5 = 0; var5 < var4; ++var5) {
                int npcId = var3[var5];
                EntityResultSet<Npc> npcResultSet = ((NpcQuery)NpcQuery.newQuery().id(npcId)).results();
                if (!npcResultSet.isEmpty()) {
                    assert Client.getLocalPlayer() != null;

                    Npc npc = (Npc)npcResultSet.nearestTo(Client.getLocalPlayer().getCoordinate());
                    if (npc != null) {
                        this.customDelayWithChance();
                        npc.interact("Interact");
                        this.waitForNpcToDisappear(npcId);
                        npcInteracted = true;
                        break;
                    }
                }
            }

            if (!npcInteracted) {
                this.println("No specified NPCs found. Retrying...");
                this.customDelay();
            }
        }

    }

    private void waitForNpcToDisappear(int npcId) {
        this.println("Waiting for NPC ID: " + npcId + " to disappear...");
        long startTime = System.currentTimeMillis();
        long timeout = this.randomIntInRange1(15000, 30000);
        long checkInterval = 5000L;
        boolean npcDisappeared = false;

        while(System.currentTimeMillis() - startTime < timeout && !npcDisappeared) {
            EntityResultSet<Npc> npcResultSet = ((NpcQuery)NpcQuery.newQuery().id(npcId)).results();
            npcDisappeared = npcResultSet.isEmpty();
            if (!npcDisappeared) {
                long timeLeft = (startTime + timeout - System.currentTimeMillis()) / 1000L;
                this.println("NPC not disappeared yet. Time left: " + timeLeft + " seconds");

                try {
                    Thread.sleep(checkInterval);
                } catch (InterruptedException var13) {
                    Thread.currentThread().interrupt();
                    this.println("Interrupted while waiting.");
                    break;
                }
            }
        }

        if (npcDisappeared) {
            this.println("NPC ID: " + npcId + " has disappeared.");
        } else {
            this.println("Timeout reached. NPC ID: " + npcId + " might still be present.");
        }

    }

    private long randomIntInRange1(int min, int max) {
        return (long)min + (long)(Math.random() * (double)(max - min + 1));
    }

    private void customDelayWithChance() {
        double chance = Math.random() * 100.0;
        int delay;
        if (chance <= 4.0) {
            delay = this.randomIntInRange(15000, 30000);
        } else if (chance <= 14.0) {
            delay = this.randomIntInRange(5000, 15000);
        } else {
            delay = this.randomIntInRange(1000, 2000);
        }

        this.println("Delaying for " + delay + "ms before next action.");

        try {
            Thread.sleep((long)delay);
        } catch (InterruptedException var5) {
            Thread.currentThread().interrupt();
            this.println("Delay interrupted.");
        }

    }

    private int randomIntInRange(int min, int max) {
        return (int)(Math.random() * (double)(max - min + 1)) + min;
    }

    private static enum HandleSharpShells {
        COLLECT_EGGS,
        HANDLE_EGGS,
        BANK_DINOSAURS;

        private HandleSharpShells() {
        }
    }

    private static enum HandleDinoArrows {
        CONFIGURE,
        SELECT_OPTION,
        PERFORM_ACTION,
        WAIT_FOR_ANIMATION_END;

        private HandleDinoArrows() {
        }
    }

    private static enum ScriptState {
        COLLECT_FROM_STORM_BARN,
        FEED_TUMMISAURUS,
        MOVE_TO_INTERMEDIATE_POINT,
        FEED_ROOTLESAURUS,
        FEED_BEANASAURUS,
        FEED_BERRISAURUS,
        USE_FERTILISER;

        private ScriptState() {
        }
    }
}
