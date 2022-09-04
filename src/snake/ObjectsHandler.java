package snake;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import java.util.concurrent.Callable;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

import snake.GameFrame.Cell;
import snake.GameFrame.Cell.colorKeys;

public class ObjectsHandler {
    // Track the objects that are present on the field
    protected static ExecutorService threadPool = Executors.newFixedThreadPool(4, 
    r -> {
        Thread t = Executors.defaultThreadFactory().newThread(r);
        t.setDaemon(true);

        return t;
    });

    // We do not need synchronization for any of these as only the table's children are modified - which are explicitly declared in 1 thread
    // Track the objects that are present on the field
    protected static ArrayList<FieldObjects> currentObjects = new ArrayList<>();

    // Store any futures that hold a delay AFTER an object has been touched
    // Ie: objects that have a certain effect for an extended duration
    protected static ArrayList<Future<?>> objectsEffects = new ArrayList<>();

    // Track the number of objects for separate object's name
    // Use atomic integer as it can occasionally be accessed asynchronously
    protected static HashMap<String, AtomicInteger> numberOfObjects = new HashMap<>();

    protected static volatile boolean isDestroying = false;

    private ObjectsHandler() {
        System.out.println("cannot instantiate the ObjectsHandler class");
    }

    public static void spawnAndQueueObjects(Callable<Coordinates> c) {
        System.out.println("Spawned objects");
        // Might not be a good idea to pass and store callables
        // The callable passed in simply find an empty position on the jframe
        new Apple(c);
        new Bomb(c);
        new DoublePoints(c);
    }

    // Detect if the snake collides with an obj, if it does, fire the .onTouched() method
    public static void checkObjectCollision(Snake s) {
        for (FieldObjects currObj : currentObjects) {
            if (currObj.checkCollision(s)) {
                break;
            }
        }   
    }

    // Check if the passed in position is already occupied by any of the objects or noit
    public static boolean checkOccupiedPosition(Coordinates pos) {
        for (FieldObjects currObj : currentObjects) {
            if (currObj.pos.equalsCoordinate(pos)) {
                return true;
            }
        }

        return false;
    }

    public static void destroyAllObjects() {
        System.out.println("Deleted objects");
        
        for (FieldObjects currObj : currentObjects) {
            currObj.destroyAndRecolor();
        }     
        currentObjects.clear();

        int childCount = 0;
        for (Future<?> effects : objectsEffects) {
            effects.cancel(true);
            childCount++;
            System.out.println(
                "Looped in objects effects: " + childCount
            );
        }
        objectsEffects.clear();

        
    }
}

abstract class FieldObjects {

    String name;
    boolean isActive = true;

    int maxAmount;
    Coordinates pos;

    static Random rand = new Random();

    // Store a collection of futures for cancellations
    ArrayList<Future<?>> futureCollections = new ArrayList<>();

    Callable<Coordinates> findPosCallback;

    static HashMap<String, AtomicInteger> numberOfObjects = ObjectsHandler.numberOfObjects;
    static Map<String, Integer> maxAmountTbl = Map.of(
        "Apple", 1,
        "Bomb", 2,
        "DoublePoints", 1
    );

    // Constructor for ordinary object
    FieldObjects(Callable<Coordinates> c, String name) {
        this.name = name;
        this.maxAmount = maxAmountTbl.get(name);

        if (!numberOfObjects.containsKey(this.name)) {
            numberOfObjects.put(this.name, new AtomicInteger(0));
        }

        findPosCallback = c;

        if (!FieldObjects.validateAmount(name, maxAmount)) { return; }

        try {
            this.pos = findPosCallback.call();
            // Color the occupied cell
            Cell.changeCellColor(pos, colorKeys.valueOf(name.toUpperCase()));
    
            ObjectsHandler.currentObjects.add(this);

            FieldObjects.addObjAmount(name, 1);

        } 
        catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Constructor for objects that hold more features such as delaying and yielded respawning
    // Time spawning: The delay time of its creation
    // Alive duration: The time required for it to respawn when not being touched (-1 to nulify)
    FieldObjects(Callable<Coordinates> c, String name, int timeSpawning, int aliveDuration) {
        this.name = name;
        this.maxAmount = maxAmountTbl.get(name);

        if (!numberOfObjects.containsKey(this.name)) {
            numberOfObjects.put(this.name, new AtomicInteger(0));
        }

        if (!FieldObjects.validateAmount(name, maxAmount)) { return; }

        // Increase the current objects present on the field
        FieldObjects.addObjAmount(name, 1);

        findPosCallback = c;

        // Create a future for cancellations
        Future<?> spawningFuture = ObjectsHandler.threadPool.submit(() -> {
            try {

                Thread.sleep(timeSpawning);

                this.pos = findPosCallback.call();

                // Color the occupied cell
                Cell.changeCellColor(pos, colorKeys.valueOf(name.toUpperCase()));
        
                ObjectsHandler.currentObjects.add(this);
                
                // Spawn another object there is room for more
                if (FieldObjects.validateAmount(name, maxAmount)) {
                    // Get the construct and create new obj with reflections
                    spawnNewSelf(c);
                    System.out.println(name + " Added by free space");
                }

                // Respawn if not touched in a period of time
                // Ignore interruptions
                if (aliveDuration > 0) {
                    Thread.sleep(aliveDuration * 1000);

                    this.destroyAndRecolor();

                    spawnNewSelf(c);

                    // TODO: Test if not interrupting this section is accurate 
                    System.out.println(name + " Added by yielding");
                }
            } catch (Exception e) {

                Thread.currentThread().interrupt();
            }
        });

        futureCollections.add(spawningFuture);
    }

    public boolean checkCollision(Snake snake) {
         if (pos.equalsCoordinate(snake.bodyPartsTbl.get(0))) {
            
            this.destroy();

            this.onTouched(snake);

            return true;
         }

         return false;
    }

    public void destroy() {
        // delete reference to garbage collect
        ObjectsHandler.currentObjects.remove(this);

        FieldObjects.addObjAmount(name, -1);
        //System.out.println("Decrement amount for: " + name);

        for (Future<?> objFuture : futureCollections) {
            // Cancel any imminent delays / actions
           objFuture.cancel(true);
        }
        futureCollections.clear();
    }

    public void destroyAndRecolor() {
        Cell.changeCellColor(pos, colorKeys.DEFAULT);

        destroy();
    }

    public void spawnNewSelf(Callable<Coordinates> c) {
        try {
            this.getClass().getDeclaredConstructor(Callable.class).newInstance(c);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static boolean validateAmount(String objName, int maxAmount) {
        AtomicInteger i = numberOfObjects.get(objName);
        return i.get() < maxAmount;
    }

    public static void addObjAmount(String objName, int incrementN) {
        AtomicInteger atomicI = numberOfObjects.get(objName);

        if (atomicI.get() >= 0) {
            atomicI.addAndGet(incrementN);
        }
    }

    protected abstract void onTouched(Snake snake);
}

class Apple extends FieldObjects {

    // The active number of all apples and the maximum amount of apples that can appear on-screen

    protected Apple(Callable<Coordinates> c) {
        super(c, "Apple");
    }
    
    @Override
    protected void onTouched(Snake snake) {
        
        snake.addLength();
        snake.scoreCounter.addScore(1);

        // Spawn another apple
        new Apple(findPosCallback);

    }
}

class Bomb extends FieldObjects {
    protected Bomb(Callable<Coordinates> c) {
        super(c, "Bomb", 
        Stats.MILISECONDS_PER_UPDATE * rand.nextInt(4, 15)
        , rand.nextInt(4, 6));
    }
    
    @Override
    protected void onTouched(Snake snake) {
        snake.decreaseLength();
        snake.scoreCounter.addScore(-1);

        new Bomb(findPosCallback);

        System.out.println(name + " Added by touching");
    }
}

class DoublePoints extends FieldObjects {
    int duration = 4;

    DoublePoints(Callable<Coordinates> c) {
        super(c, "DoublePoints"
        , Stats.MILISECONDS_PER_UPDATE * rand.nextInt(10, 15)
        , rand.nextInt(4, 6));
    }

    @Override
    protected void onTouched(Snake snake) {
        snake.scoreCounter.setDoubleScore(true);

        Future<?> doublePointsFuture = ObjectsHandler.threadPool.submit(() -> {
            try {
                // Get the index that will be inserted to
                int futureIndex = ObjectsHandler.objectsEffects.size();

                Thread.sleep(4 * 1000);
    
                snake.scoreCounter.setDoubleScore(false);
                new DoublePoints(findPosCallback);

                ObjectsHandler.objectsEffects.remove(futureIndex);
            } catch (InterruptedException e) {
    
                Thread.currentThread().interrupt();
    
            } 

        });

        ObjectsHandler.objectsEffects.add(doublePointsFuture);
    }
}