package net.botwithus;

import net.botwithus.api.game.hud.Dialog;
import net.botwithus.api.game.hud.inventories.Backpack;
import net.botwithus.internal.scripts.ScriptDefinition;
import net.botwithus.rs3.events.EventBus;
import net.botwithus.rs3.events.impl.InventoryUpdateEvent;
import net.botwithus.rs3.game.Client;
import net.botwithus.rs3.game.Coordinate;
import net.botwithus.rs3.game.Item;
import net.botwithus.rs3.game.hud.interfaces.Component;
import net.botwithus.rs3.game.hud.interfaces.Interfaces;
import net.botwithus.rs3.game.js5.types.vars.VarDomainType;
import net.botwithus.rs3.game.minimenu.MiniMenu;
import net.botwithus.rs3.game.minimenu.actions.ComponentAction;
import net.botwithus.rs3.game.movement.Movement;
import net.botwithus.rs3.game.movement.NavPath;
import net.botwithus.rs3.game.queries.builders.characters.NpcQuery;
import net.botwithus.rs3.game.queries.builders.components.ComponentQuery;
import net.botwithus.rs3.game.queries.builders.items.InventoryItemQuery;
import net.botwithus.rs3.game.queries.builders.objects.SceneObjectQuery;
import net.botwithus.rs3.game.queries.results.EntityResultSet;
import net.botwithus.rs3.game.queries.results.ResultSet;
import net.botwithus.rs3.game.scene.entities.characters.npc.Npc;
import net.botwithus.rs3.game.scene.entities.characters.player.LocalPlayer;
import net.botwithus.rs3.game.scene.entities.object.SceneObject;
import net.botwithus.rs3.game.skills.Skills;
import net.botwithus.rs3.game.vars.VarManager;
import net.botwithus.rs3.script.Execution;
import net.botwithus.rs3.script.LoopingScript;
import net.botwithus.rs3.script.config.ScriptConfig;
import net.botwithus.rs3.util.RandomGenerator;

import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

import static net.botwithus.rs3.game.Client.getLocalPlayer;

public class SkeletonScript extends LoopingScript {

    private LocalPlayer player = Client.getLocalPlayer();
    boolean CollectEggs;
    boolean CollectFertiliser;
    boolean Zygomite;
    boolean cutOption;
    boolean dryOption;
    boolean cutAndDryOption;
    private boolean scriptRunning = false;
    private Instant scriptStartTime;
    private boolean isCollectingEggs = false;
    private ScriptState currentState = ScriptState.IDLE;
    private int startingFiremakingLevel;
    public int getStartingFiremakingLevel() {
        return startingFiremakingLevel;
    }
    private final Coordinate INCUBATOR_LOCATION = new Coordinate(5208, 2352, 0);
    private final Coordinate COMPOST_LOCATION = new Coordinate(5228, 2326, 0);
    private final Coordinate PILE_OF_EGGS_LOCATION = new Coordinate(5219, 2349, 0);
    private final String[] dinosaurNames = {
            "Feral dinosaur (unchecked)", "Venomous dinosaur (unchecked)", "Ripper dinosaur (unchecked)",
            "Brutish dinosaur (unchecked)", "Arcane apoterrasaur (unchecked)", "Scimitops (unchecked)",
            "Bagrada rex (unchecked)", "Spicati apoterrasaur (unchecked)", "Asciatops (unchecked)",
            "Corbicula rex (unchecked)", "Oculi apoterrasaur (unchecked)", "Malletops (unchecked)",
            "Pavosaurus rex (unchecked)"
    };

    public void startScript() {
        println("Attempting to start script...");
        if (!scriptRunning) {
            scriptRunning = true;
            scriptStartTime = Instant.now();
            println("Script started at: " + scriptStartTime);
        } else {
            println("Attempted to start script, but it is already running.");
        }
    }

    public void stopScript() {
        if (scriptRunning) {
            scriptRunning = false;
            Instant stopTime = Instant.now();
            println("Script stopped at: " + stopTime);
            long duration = Duration.between(scriptStartTime, stopTime).toMillis();
            println("Script ran for: " + duration + " milliseconds.");
        } else {
            println("Attempted to stop script, but it is not running.");
        }
    }

    enum ScriptState {
        IDLE,
        NAVIGATING_TO_EGGS,
        COLLECTING_EGGS,
        HANDLING_GOOD_EGGS,
        HANDLING_BAD_EGGS,
        DEPOSITING_EGGS,
        DEPOSITING_DINOSAURS,
        INITIAL_STATE,
        NAVIGATING_TO_BARN,
        COLLECTING_ANIMALS,
        TUMMISAURUS,
        ROOTLESAURUS,
        BEANASAURUS,
        BERRISAURUS,
        USE_FERTILISER,
        NAVIGATING_TO_ZYGOMITE,
        CHOOSE_OPTION,
        INTERACT_WITH_ZYGOMITE,


    }

    public SkeletonScript(String s, ScriptConfig scriptConfig, ScriptDefinition scriptDefinition) {
        super(s, scriptConfig, scriptDefinition);
        this.sgc = new SkeletonScriptGraphicsContext(getConsole(), this);
    }

    @Override
    public void onLoop() {
        if (getLocalPlayer() != null && Client.getGameState() == Client.GameState.LOGGED_IN) {

            if (!scriptRunning) {
                return;
            }
        }
        switch (currentState) {
            case IDLE:
                idle();
                break;
            case NAVIGATING_TO_EGGS:
                moveToEggs();
                break;
            case COLLECTING_EGGS:
                collectEggs();
                break;
            case HANDLING_GOOD_EGGS:
                goodEggs();
                break;
            case HANDLING_BAD_EGGS:
                badEggs();
                break;
            case DEPOSITING_DINOSAURS:
                handleDinosaurs();
                break;
            case INITIAL_STATE:
                determineInitialState();
                break;
            case NAVIGATING_TO_BARN:
                moveToBarn();
                break;
            case COLLECTING_ANIMALS:
                collectAnimals();
                break;
            case TUMMISAURUS:
                tummisaurus();
                break;
            case ROOTLESAURUS:
                rootlesaurus();
                break;
            case BEANASAURUS:
                beanasaurus();
                break;
            case BERRISAURUS:
                berrisaurus();
                break;
            case USE_FERTILISER:
                useFertiliser();
                break;
            case NAVIGATING_TO_ZYGOMITE:
                moveToZygomite();
                break;
            case CHOOSE_OPTION:
                chooseOption();
                break;
            case INTERACT_WITH_ZYGOMITE:
                interactBasedOnOption();
                break;
        }

        if (CollectEggs) {
            checkAndSwitchStates();
        }
        Execution.delay(RandomGenerator.nextInt(800, 1200));
    }

    private void navigateTo(Coordinate destination) {
        if (Client.getLocalPlayer() == null) {
            return;
        }
        println("Navigating to " + destination);
        Movement.traverse(NavPath.resolve(destination));
        Execution.delayUntil(360000, () -> destination.equals(Client.getLocalPlayer().getCoordinate()));
    }

    private void idle() {
        println("Work, work...");
        Execution.delay(RandomGenerator.nextInt(5000, 10000));
        if (CollectEggs) {
            currentState = ScriptState.NAVIGATING_TO_EGGS;
        } else if (CollectFertiliser) {
            currentState = ScriptState.NAVIGATING_TO_BARN;
        } else if (Zygomite) {
            currentState = ScriptState.NAVIGATING_TO_ZYGOMITE;
        }
    }
    private void moveToEggs() {
        SceneObject mysticalTreeObject = SceneObjectQuery.newQuery().name("Mystical tree").results().nearest();
        Coordinate mysticalTree = new Coordinate(5301, 2293, 0);
        Coordinate randomisedmysticalTree = randomizeLocation(mysticalTree, 1);


        if (player.getCoordinate().getRegionId() == 20772) {
            println("Already in the Mystical Tree region.");
            checkAndSwitchStates();
        } else {
            println("Navigating to Mystical Tree");
            navigateTo(randomisedmysticalTree);

            if (mysticalTreeObject != null) {
                if (mysticalTreeObject.interact("Teleport")) {
                    println("Teleported using Mystical Tree");
                    Execution.delay(RandomGenerator.nextInt(3000, 5000));
                    if (Interfaces.isOpen(1188)) {
                        println("Teleported to Mystical Tree.");
                        Execution.delay(RandomGenerator.nextInt(3000, 5000));
                        Dialog.interact("Ben's carestyling salon.");
                        Execution.delay(RandomGenerator.nextInt(3000, 5000));
                        checkAndSwitchStates();
                    }
                }
            }
        }
    }

    private boolean isNearLocation(Coordinate playerLocation, Coordinate targetLocation, int threshold) {
        int deltaX = playerLocation.getX() - targetLocation.getX();
        int deltaY = playerLocation.getY() - targetLocation.getY();
        double distance = Math.sqrt(deltaX * deltaX + deltaY * deltaY);

        return distance <= threshold;
    }

    private boolean isNearIncubator() {
        return isNearLocation(player.getCoordinate(), INCUBATOR_LOCATION, 25);
    }

    private boolean isNearCompost() {
        return isNearLocation(player.getCoordinate(), COMPOST_LOCATION, 25);
    }

    private boolean isNearPileOfEggs() {
        return isNearLocation(player.getCoordinate(), PILE_OF_EGGS_LOCATION, 25);
    }

    private void checkAndSwitchStates() {
        if (Backpack.isFull()) {
            handleFullBackpack();
        } else if (isNearPileOfEggs()) {
            currentState = ScriptState.COLLECTING_EGGS;
        } else if (isNearIncubator() && countItems("Good egg") > 0) {
            currentState = ScriptState.HANDLING_GOOD_EGGS;
        } else if (isNearCompost() && countItems("Bad egg") > 0) {
            currentState = ScriptState.HANDLING_BAD_EGGS;
        }
        switch (currentState) {
            case COLLECTING_EGGS:
                if (Backpack.isFull()) {
                    currentState = countItems("Good egg") > countItems("Bad egg") ?
                            ScriptState.HANDLING_GOOD_EGGS : ScriptState.HANDLING_BAD_EGGS;
                }
                break;
            case HANDLING_GOOD_EGGS:
                if (!Backpack.contains("Good egg")) {
                    currentState = Backpack.isFull() ? ScriptState.DEPOSITING_EGGS : ScriptState.COLLECTING_EGGS;
                }
                break;
            case HANDLING_BAD_EGGS:
                if (!Backpack.contains("Bad egg")) {
                    currentState = Backpack.isFull() ? ScriptState.DEPOSITING_EGGS : ScriptState.COLLECTING_EGGS;
                }
                break;
            case DEPOSITING_EGGS:
                currentState = ScriptState.COLLECTING_EGGS;
                break;
        }
    }

    private void handleFullBackpack() {
        if (containsAnyDinosaur()) {
            currentState = ScriptState.DEPOSITING_DINOSAURS;
        } else {
            currentState = countItems("Good egg") > countItems("Bad egg") ?
                    ScriptState.HANDLING_GOOD_EGGS : ScriptState.HANDLING_BAD_EGGS;
        }
    }

    private int countItems(String itemName) {
        return Backpack.getCount(itemName);
    }

    private void collectEggs() {
        if (currentState != ScriptState.COLLECTING_EGGS) return;
        isCollectingEggs = true;
        if (!Backpack.isFull()) {
            println("Collecting eggs...");
            SceneObjectQuery.newQuery().name("Pile of dinosaur eggs").results().nearest().interact("Collect");
            Execution.delayUntil(60000, Backpack::isFull);
            Execution.delay(RandomGenerator.nextInt(800, 1200));
        }
        isCollectingEggs = false;
    }

    private void badEggs() {
        if (currentState != ScriptState.HANDLING_BAD_EGGS) return;
        EntityResultSet<SceneObject> badEggs = SceneObjectQuery.newQuery().id(123389).results();
        if (!badEggs.isEmpty()) {
            println("Composting bad eggs...");
            Objects.requireNonNull(badEggs.nearest()).interact("Compost");
            Execution.delayUntil(60000, () -> !Backpack.contains("Bad egg"));
            Execution.delay(RandomGenerator.nextInt(800, 1200));
        }
        if (!Backpack.isFull()) {
            currentState = ScriptState.COLLECTING_EGGS;
        } else {
            currentState = ScriptState.DEPOSITING_EGGS;
        }
    }

    private void goodEggs() {
        if (currentState != ScriptState.HANDLING_GOOD_EGGS) return;
        EntityResultSet<SceneObject> goodEggs = SceneObjectQuery.newQuery().id(123386).results();
        if (!goodEggs.isEmpty()) {
            println("Incubating good eggs...");
            Objects.requireNonNull(goodEggs.nearest()).interact("Incubate");
            Execution.delayUntil(60000, () -> !Backpack.contains("Good egg"));
            Execution.delay(RandomGenerator.nextInt(800, 1200));
        }
        if (!Backpack.isFull()) {
            currentState = ScriptState.COLLECTING_EGGS;
        } else {
            currentState = ScriptState.DEPOSITING_EGGS;
        }
    }

    private boolean containsAnyDinosaur() {
        for (String dinosaur : dinosaurNames) {
            if (Backpack.contains(dinosaur)) {
                return true;
            }
        }
        return false;
    }

    private void handleDinosaurs() {
        if (!containsAnyDinosaur()) {
            return;
        }

        println("Depositing Dinosaurs...");
        Coordinate bankLocation = new Coordinate(5202, 2367, 0);
        Coordinate randomisedBankLocation = randomizeLocation(bankLocation, 0);
        navigateTo(randomisedBankLocation);
        SceneObjectQuery.newQuery().name("Bank chest").results().nearest().interact("Use");
        Execution.delayUntil(5000, () -> Interfaces.isOpen(517));
        Execution.delay(RandomGenerator.nextInt(2000, 3000));

        AtomicBoolean depositedAny = new AtomicBoolean(false);

        for (String dinosaur : dinosaurNames) {
            if (containsAnyDinosaur()) {
                ComponentQuery.newQuery(517).componentIndex(15).itemName(dinosaur).results().forEach((component) -> {
                    String depositAction = this.findDepositAction(component);
                    if (depositAction != null) {
                        component.interact(depositAction);
                        Execution.delay(RandomGenerator.nextInt(1000, 1500));
                        depositedAny.set(true);
                        Execution.delay(RandomGenerator.nextInt(1000, 1500));
                    }

                });
            }
        }

        if (depositedAny.get()) {
            println("Dinosaurs have been deposited.");
        } else {
            println("No dinosaurs to deposit.");
        }
        println("Back to work.");
        checkAndSwitchStates();
    }

    private String findDepositAction(Component component) {
        Iterator<String> var2 = component.getOptions().iterator();

        String option;
        do {
            if (!var2.hasNext()) {
                return null;
            }

            option = var2.next();
        } while (!option.startsWith("Deposit"));

        return option;
    }

    private Coordinate randomizeLocation(Coordinate original, int maxOffset) {
        Random random = new Random();
        int xAdjustment = random.nextInt(maxOffset * 2 + 1) - maxOffset;
        int yAdjustment = random.nextInt(maxOffset * 2 + 1) - maxOffset;
        return new Coordinate(original.getX() + xAdjustment, original.getY() + yAdjustment, original.getZ());
    }

    private void moveToBarn() {
        Coordinate barnLocation = new Coordinate(5303, 2276, 0);
        Coordinate randomisedBarnLocation = randomizeLocation(barnLocation, 1);
        if (player.getCoordinate().getRegionId() == 21027 || player.getCoordinate().getRegionId() == 21283) {
            println("Already in at the Correct Location.");
            currentState = ScriptState.INITIAL_STATE;
        } else {
            println("Navigating to Correct Location");
            navigateTo(randomisedBarnLocation);
            currentState = ScriptState.INITIAL_STATE;
        }
    }

    private void determineInitialState() {
        if (Backpack.contains("Potterington Blend #102 Fertiliser")) {
            currentState = ScriptState.USE_FERTILISER;
        } else if (Backpack.contains("Tummisaurus Rex")) {
            currentState = ScriptState.TUMMISAURUS;
        } else if (Backpack.contains("Rootlesaurus Rex")) {
            currentState = ScriptState.ROOTLESAURUS;
        } else if (Backpack.contains("Beanasaurus Rex")) {
            currentState = ScriptState.BEANASAURUS;
        } else if (Backpack.contains("Berrisaurus Rex")) {
            currentState = ScriptState.BERRISAURUS;
        } else {
            currentState = ScriptState.COLLECTING_ANIMALS;
        }
    }

    private void collectAnimals() {
        EntityResultSet<SceneObject> StormBarn = SceneObjectQuery.newQuery().name("Storm barn").results();
        if (StormBarn.isEmpty()) {
            Coordinate stormBarnLocation = new Coordinate(5305, 2280, 0);
            Coordinate randomisedStormBarnLocation = randomizeLocation(stormBarnLocation, 2);
            Movement.walkTo(randomisedStormBarnLocation.getX(), randomisedStormBarnLocation.getY(), true);
            Execution.delayUntil(60000, () -> isNearLocation(player.getCoordinate(), randomisedStormBarnLocation, 5));
            if (SceneObjectQuery.newQuery().name("Storm barn").results().nearest().interact("Collect")) {
                Execution.delayUntil(60000, Backpack::isFull);
                Execution.delay(RandomGenerator.nextInt(800, 1200));
            }
        } else {
            println("Collecting animals...");
            SceneObjectQuery.newQuery().name("Storm barn").results().nearest().interact("Collect");
            Execution.delayUntil(60000, Backpack::isFull);
            Execution.delay(RandomGenerator.nextInt(800, 1200));
            currentState = ScriptState.TUMMISAURUS;
        }
    }

    private void tummisaurus() {
        EntityResultSet<SceneObject> tummisaurus = SceneObjectQuery.newQuery().name("Rooty mush trough").results();
        Coordinate tummisaurusLocation = new Coordinate(5289, 2259, 0);
        Coordinate randomisedTummisaurusLocation = randomizeLocation(tummisaurusLocation, 1);
        if (tummisaurus.isEmpty()) {
            Movement.walkTo(randomisedTummisaurusLocation.getX(), randomisedTummisaurusLocation.getY(), true);
            Execution.delayUntil(60000, () -> isNearLocation(player.getCoordinate(), randomisedTummisaurusLocation, 10));
            if (SceneObjectQuery.newQuery().name("Rooty mush trough").results().nearest().interact("Feed")) {
                Execution.delayUntil(60000, () -> !Backpack.contains("Tummisaurus Rex"));
                Execution.delay(RandomGenerator.nextInt(800, 1200));
            }
        } else {
            println("Feeding tummisaurus...");
            Objects.requireNonNull(tummisaurus.nearest()).interact("Feed");
            Execution.delayUntil(60000, () -> !Backpack.contains("Tummisaurus Rex"));
            Execution.delay(RandomGenerator.nextInt(800, 1200));
        }
        currentState = ScriptState.ROOTLESAURUS;
    }

    private void rootlesaurus() {
        EntityResultSet<SceneObject> rootlesaurus = SceneObjectQuery.newQuery().name("Beany mush trough").results();
        Coordinate rootlesaurusLocation = new Coordinate(5311, 2293, 0);
        Coordinate randomisedrootlesaurusLocation = randomizeLocation(rootlesaurusLocation, 2);
        if (rootlesaurus.isEmpty()) {
            Movement.walkTo(randomisedrootlesaurusLocation.getX(), randomisedrootlesaurusLocation.getY(), true);
            Execution.delayUntil(60000, () -> isNearLocation(player.getCoordinate(), randomisedrootlesaurusLocation, 5));
            println("Feeding rootlesaurus...");
            if (SceneObjectQuery.newQuery().name("Beany mush trough").results().nearest().interact("Feed")) {
                Execution.delayUntil(60000, () -> !Backpack.contains("Rootlesaurus Rex"));
                Execution.delay(RandomGenerator.nextInt(800, 1200));
            }

        } else {
            println("Feeding rootlesaurus...");
            Objects.requireNonNull(rootlesaurus.nearest()).interact("Feed");
            Execution.delayUntil(60000, () -> !Backpack.contains("Rootlesaurus Rex"));
            Execution.delay(RandomGenerator.nextInt(800, 1200));
        }
        currentState = ScriptState.BEANASAURUS;
    }

    private void beanasaurus() {
        EntityResultSet<SceneObject> beanasaurus = SceneObjectQuery.newQuery().name("Berry mush trough").results();
        Coordinate beanasaurusLocation = new Coordinate(5329, 2289, 0);
        Coordinate randomisedbeanasaurusLocation = randomizeLocation(beanasaurusLocation, 2);
        if (beanasaurus.isEmpty()) {
            Movement.walkTo(randomisedbeanasaurusLocation.getX(), randomisedbeanasaurusLocation.getY(), true);
            Execution.delayUntil(60000, () -> isNearLocation(player.getCoordinate(), randomisedbeanasaurusLocation, 5));
            if (SceneObjectQuery.newQuery().name("Berry mush trough").results().nearest().interact("Feed")) {
                Execution.delayUntil(60000, () -> !Backpack.contains("Beanasaurus Rex"));
                Execution.delay(RandomGenerator.nextInt(800, 1200));
            }
        } else {
            println("Feeding beanasaurus...");
            Objects.requireNonNull(beanasaurus.nearest()).interact("Feed");
            Execution.delayUntil(60000, () -> !Backpack.contains("Beanasaurus Rex"));
            Execution.delay(RandomGenerator.nextInt(800, 1200));
        }
        currentState = ScriptState.BERRISAURUS;
    }

    private void berrisaurus() {
        EntityResultSet<SceneObject> berrisaurus = SceneObjectQuery.newQuery().name("Cerealy mush trough").results();
        Coordinate berrisaurusLocation = new Coordinate(5329, 2269, 0);
        Coordinate randomisedberrisaurusLocation = randomizeLocation(berrisaurusLocation, 2);
        if (berrisaurus.isEmpty()) {
            Movement.walkTo(randomisedberrisaurusLocation.getX(), randomisedberrisaurusLocation.getY(), true);
            Execution.delayUntil(60000, () -> isNearLocation(player.getCoordinate(), randomisedberrisaurusLocation, 5));
             if (SceneObjectQuery.newQuery().name("Cerealy mush trough").results().nearest().interact("Feed")) {
                 Execution.delayUntil(60000, () -> !Backpack.contains("Berrisaurus Rex"));
                 Execution.delay(RandomGenerator.nextInt(800, 1200));
             }
        } else {
            println("Feeding berrisaurus...");
            Objects.requireNonNull(berrisaurus.nearest()).interact("Feed");
            Execution.delayUntil(60000, () -> !Backpack.contains("Berrisaurus Rex"));
            Execution.delay(RandomGenerator.nextInt(800, 1200));
        }
        currentState = ScriptState.USE_FERTILISER;
    }

    private void useFertiliser() {
        ResultSet<Item> fertiliserItems = InventoryItemQuery.newQuery(93).name("Potterington Blend #102 Fertiliser").results();
        if (!fertiliserItems.isEmpty()) {
            println("Using fertiliser...");

            Item firstFertiliser = fertiliserItems.first();
            if (firstFertiliser != null) {
                Backpack.interact("Potterington Blend #102 Fertiliser", "Ignite");
                Execution.delayUntil(60000, () -> !Backpack.contains("Potterington Blend #102 Fertiliser"));
                Execution.delay(RandomGenerator.nextInt(800, 1200));
                currentState = ScriptState.COLLECTING_ANIMALS;
            }
        }
    }

    private void moveToZygomite() {
        SceneObject mysticalTreeObject = SceneObjectQuery.newQuery().name("Mystical tree").results().nearest();
        Coordinate mysticalTree = new Coordinate(5301, 2293, 0);
        Coordinate randomisedmysticalTree = randomizeLocation(mysticalTree, 1);


        if (player.getCoordinate().getRegionId() == 20772) {
            println("Already in the Correct Location.");
            currentState = ScriptState.CHOOSE_OPTION;
        } else {
            println("Navigating to Mystical Tree");
            navigateTo(randomisedmysticalTree);

            if (mysticalTreeObject != null) {
                if (mysticalTreeObject.interact("Teleport")) {
                    println("Teleported using Mystical Tree");
                    Execution.delay(RandomGenerator.nextInt(3000, 5000));
                    if (Interfaces.isOpen(1188)) {
                        println("Teleported to Mystical Tree.");
                        Execution.delay(RandomGenerator.nextInt(3000, 5000));
                        Dialog.interact("Ben's carestyling salon.");
                        Execution.delay(RandomGenerator.nextInt(3000, 5000));
                        currentState = ScriptState.CHOOSE_OPTION;
                    }
                }
            }
        }
    }

    private void chooseOption() {
        String selectedOption = selectActionOnInterface();

        if ("Cut".equals(selectedOption) && VarManager.getVarValue(VarDomainType.PLAYER, 10431) == 9306123 ||
                "Dry".equals(selectedOption) && VarManager.getVarValue(VarDomainType.PLAYER, 10431) == 9371659 ||
                "Cut and Dry".equals(selectedOption) && VarManager.getVarValue(VarDomainType.PLAYER, 10431) == 9240587) {
            println("Selected option is already active. Skipping Zygomite interaction.");
            currentState = ScriptState.INTERACT_WITH_ZYGOMITE;
            return;
        }

        EntityResultSet<Npc> scruffies = NpcQuery.newQuery().name("Scruffy zygomite").results();
        if (!scruffies.isEmpty()) {
            Npc scruffy = scruffies.nearestTo(player.getCoordinate());
            if (scruffy != null) {
                boolean selectionSuccess = scruffy.interact("Configure");
                if (selectionSuccess) {
                    Execution.delay(RandomGenerator.nextInt(2500, 3500));
                    if (Interfaces.isOpen(720)) {
                        selectedOption = selectActionOnInterface();
                        println("Option selected: " + selectedOption);
                        Execution.delayUntil(5000, () -> !Interfaces.isOpen(720));
                        Execution.delay(RandomGenerator.nextInt(1000, 2000));
                        currentState = ScriptState.INTERACT_WITH_ZYGOMITE;
                    } else {
                        println("Failed to open expected interface after interacting with Scruffy Zygomite.");
                    }
                } else {
                    println("Failed to interact with Scruffy Zygomite.");
                }
            }
        } else {
            println("Scruffy Zygomite not found.");
        }
    }

    private String selectActionOnInterface() {
        String selectedOption = "";

        if (cutOption) {
            selectedOption = "Cut";
            MiniMenu.interact(ComponentAction.DIALOGUE.getType(), 0, -1, 47185940);
        } else if (dryOption) {
            selectedOption = "Dry";
            MiniMenu.interact(ComponentAction.DIALOGUE.getType(), 0, -1, 47185943);
        } else if (cutAndDryOption) {
            selectedOption = "Cut and Dry";
            MiniMenu.interact(ComponentAction.DIALOGUE.getType(), 0, -1, 47185921);
        } else {
            println("No valid option selected for Scruffy Zygomite.");
            selectedOption = "";
        }

        return selectedOption;
    }

    private void interactBasedOnOption() {
        if (cutOption) {
            interactWithSpecificNpc("Cut");
        } else if (dryOption) {
            interactWithSpecificNpc("Dry");
        } else if (cutAndDryOption) {
            interactWithSpecificNpc("Cut and dry");
        }
    }

    private void interactWithSpecificNpc(String action) {
        List<Coordinate> zygomiteLocations = Arrays.asList(
                new Coordinate(5232, 2326, 0),
                new Coordinate(5234, 2327, 0),
                new Coordinate(5236, 2328, 0)
        );

        Collections.shuffle(zygomiteLocations);

        for (Coordinate location : zygomiteLocations) {
            Optional<Npc> scruffy = findScruffyZygomiteAtLocation(location);

            if (scruffy.isPresent() && scruffy.get().getOptions().contains(action)) {
                boolean interactionSuccess = scruffy.get().interact(action);
                if (interactionSuccess) {
                    println("Interacted with Scruffy Zygomite using " + action + ".");
                    Execution.delay(RandomGenerator.nextInt(2500, 3500));
                    Execution.delayUntil(120000, () -> player.getOverheadText().equals("Another satisfied customer!"));
                    Execution.delay(RandomGenerator.nextInt(800, 1200));
                    break;
                } else {
                    println("Failed to interact with Scruffy Zygomite at " + location + ".");
                }
            }
        }
    }

    private Optional<Npc> findScruffyZygomiteAtLocation(Coordinate location) {
        EntityResultSet<Npc> npcs = NpcQuery.newQuery().name("Scruffy zygomite").results();

        return npcs.stream()
                .filter(npc -> npc.getCoordinate().equals(location))
                .findFirst();
    }
}
//test