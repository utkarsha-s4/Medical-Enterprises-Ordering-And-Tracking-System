package Mini_project;
import java.util.*;
import java.time.Instant;
import java.text.DecimalFormat;
import java.time.Duration;

class Order {
    String name;
    int age;
    int id;
    int priority;
    Tracking tracking;

    public Order(String name, int priority, int age, int id, Tracking tracking) {
        this.name = name;
        this.age = age;
        this.id = id;
        this.priority = priority;
        this.tracking = tracking;
    }
}

class Tracking {
    enum Status { ORDERED, IN_PROGRESS, DELIVERED }
    Status status;
    Instant statusTimestamp;

    public Tracking() {
        status = Status.ORDERED;
        statusTimestamp = Instant.now();
    }

    public void updateStatus(Status newStatus) {
        Instant currentTime = Instant.now();
        // Check if 30 seconds have elapsed since the last status change
        if (Duration.between(statusTimestamp, currentTime).getSeconds() >= 30) {
            status = newStatus;
            statusTimestamp = currentTime;
        }
    }

    public Status getStatus() {
        return status;
    }
}

class Item {
    String name;
    double purchasePrice;
    double rentalPricePerHour;

    public Item(String name, double purchasePrice, double rentalPricePerHour) {
        this.name = name;
        this.purchasePrice = purchasePrice;
        this.rentalPricePerHour = rentalPricePerHour;
    }
}

class OrderItem {
    Item item;
    int quantity;

    public OrderItem(Item item, int quantity) {
        this.item = item;
        this.quantity = quantity;
    }
}

class OrderMenu {
    private HashMap<String, Item> menuItems;

    public OrderMenu() {
        menuItems = new HashMap<>();
        // Initialize the menu items, including rental prices per hour
        menuItems.put("Oxygen Cylinder (10 ltr)", new Item("Oxygen Cylinder (10 ltr)", 4500.0, 200.0));
        menuItems.put("Medical Ventilator", new Item("Medical Ventilator", 120000.0,829.0));
        menuItems.put("Blood pressure monitoring device", new Item("Blood pressure monitoring device", 849.0, 40.0));
        menuItems.put("ECG machine", new Item("ECG machine", 17700.0, 500.0));
        menuItems.put("Patient Monitor", new Item("Patient Monitor", 12999.0, 450.0));
        menuItems.put("BPL Oximeter", new Item("BPL Oximeter", 1269.0, 300.0));
        menuItems.put("Nebulizer", new Item("Nebulizer", 9599.0, 200.0));
    }

    public void displayMenu() {
        System.out.println("Menu:");
        for (String itemName : menuItems.keySet()) {
            Item item = menuItems.get(itemName);
            System.out.println(itemName + " - Rs." + item.purchasePrice + " (Purchase) / Rs." + item.rentalPricePerHour + " per hour (Rent)");
        }
    }

    public Item getItemByName(String itemName) {
        return menuItems.get(itemName);
    }
}

class PriorityQueue {
    public HashMap<Integer, LinkedList<Order>> priorityMap;

    public PriorityQueue() {
        priorityMap = new HashMap<>();
        // Start a background thread to periodically update order statuses
        startStatusUpdaterThread();
    }

    private void startStatusUpdaterThread() {
        Thread statusUpdaterThread = new Thread(() -> {
            while (true) {
                try {
                    Thread.sleep(1000); // Sleep for 1 second
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                updateOrderStatuses();
            }
        });
        statusUpdaterThread.setDaemon(true);
        statusUpdaterThread.start();
    }

    private void updateOrderStatuses() {
        Instant currentTime = Instant.now();
        for (int priority : priorityMap.keySet()) {
            LinkedList<Order> orders = priorityMap.get(priority);
            for (Order order : orders) {
                if (order.tracking.getStatus() == Tracking.Status.ORDERED) {
                    // Check if 30 seconds have elapsed since the order was placed
                    if (Duration.between(order.tracking.statusTimestamp, currentTime).getSeconds() >= 30) {
                        order.tracking.updateStatus(Tracking.Status.IN_PROGRESS);
                    }
                }
            }
        }
    }

    public void enqueue(Order order) {
        int priority = order.priority;
        if (!priorityMap.containsKey(priority)) {
            priorityMap.put(priority, new LinkedList<>());
        }
        priorityMap.get(priority).add(order);
    }

    public Order dequeue() {
        int highestPriority = priorityMap.isEmpty() ? -1 : Collections.min(priorityMap.keySet());
        if (highestPriority == -1) {
            System.out.println("Queue is empty");
            return null;
        }
        LinkedList<Order> orders = priorityMap.get(highestPriority);
        Order removedOrder = orders.poll();
        if (orders.isEmpty()) {
            priorityMap.remove(highestPriority);
        }
        return removedOrder;
    }

    public void display() {
        if (priorityMap.isEmpty()) {
            System.out.println("Queue is empty");
            return;
        }

        System.out.println("Orders in Priority Queue:");
        System.out.println("+---------------------------------------------------------------+");
        System.out.println("| Order Name                                         | Priority |");
        System.out.println("+---------------------------------------------------------------+");

        for (int priority : priorityMap.keySet()) {
            LinkedList<Order> orders = priorityMap.get(priority);
            for (Order order : orders) {
                String orderName = order.name;
                String priorityString = String.valueOf(order.priority);

                // Pad the strings to align them in the table
                orderName = padString(orderName, 30);
                priorityString = padString(priorityString, 9);

                System.out.println("| " + orderName + "                    | " + priorityString + " |");
            }
        }

        System.out.println("+---------------------------------------------------------------+");
    }

    private String padString(String input, int width) {
        int spacesToAdd = width - input.length();
        if (spacesToAdd > 0) {
            StringBuilder paddedString = new StringBuilder(input);
            for (int i = 0; i < spacesToAdd; i++) {
                paddedString.append(" ");
            }
            return paddedString.toString();
        }
        return input;
    }
}

public class Main {
    private static void checkOrderStatus(PriorityQueue priorityQueue, int orderToTrackID) {
        boolean orderFound = false;

        for (int priority : priorityQueue.priorityMap.keySet()) {
            LinkedList<Order> orders = priorityQueue.priorityMap.get(priority);
            for (Order order : orders) {
                if (order.id == orderToTrackID) {
                    System.out.println("Order ID: " + order.id);
                    System.out.println("Order Name: " + order.name);
                    System.out.println("Order Status: " + order.tracking.getStatus());

                    // Check if the status is DELIVERED
                    if (order.tracking.getStatus() == Tracking.Status.DELIVERED) {
                        System.out.println("This order has been DELIVERED.");
                    } else if (order.tracking.getStatus() == Tracking.Status.IN_PROGRESS) {
                        System.out.println("This order is currently IN_PROGRESS.");
                    }

                    orderFound = true;
                    break;
                }
            }
            if (orderFound) {
                break;
            }
        }

        if (!orderFound) {
            System.out.println("Order with ID " + orderToTrackID + " not found.");
        }
    }

    public static void main(String[] args) {
        PriorityQueue priorityQueue = new PriorityQueue();
        Scanner scanner = new Scanner(System.in);
        OrderMenu orderMenu = new OrderMenu();

        while (true) {
            System.out.println("************************WELCOME TO OUR HEALTHCARE STORE**************************");
            System.out.println("-----------Here is our Menu----------");
            System.out.println("1. Place an order");
            System.out.println("2. Check delivery status");
            System.out.println("3. Check order status");
            System.out.println("4. Exit");
            System.out.print("Enter your choice: ");

            int choice;
            try {
                choice = scanner.nextInt();
                scanner.nextLine();  // Consume the newline character
            } catch (InputMismatchException e) {
                System.out.println("Invalid input. Please enter a number.");
                scanner.nextLine();  // Consume the invalid input
                continue;
            }

            int age, id, orderPriority = 0;
            String orderName = "";
            double totalCost = 0; // Initialize total cost to 0

            switch (choice) {
                case 1:
                    System.out.println("Kindly enter Id of the patient (last 4 digits of Aadhar card):");
                    id = scanner.nextInt();
                    System.out.println("Kindly enter age of the patient:");
                    age = scanner.nextInt();
                    scanner.nextLine(); // Consume the newline character
                    System.out.println("Place an order from the following displayed menu:");
                    orderMenu.displayMenu();
                    String orderConfirmation = "";
                    Tracking tracking = new Tracking();
                    tracking.updateStatus(Tracking.Status.IN_PROGRESS); // Set status to IN_PROGRESS
                    System.out.println("Place an order:");
                    List<OrderItem> orderItems = new ArrayList<>();
                    do {
                        System.out.print("Enter the item name to add to the order: ");
                        String itemName = scanner.nextLine();
                        Item item = orderMenu.getItemByName(itemName);
                        if (item != null) {
                            System.out.print("Select order type (1. Purchase, 2. Rent): ");
                            int orderTypeChoice = 0;
                            try {
                                orderTypeChoice = scanner.nextInt();
                            } catch (InputMismatchException e) {
                                System.out.println("Invalid input. Please enter 1 for Purchase or 2 for Rent.");
                                scanner.nextLine();
                                continue;
                            }
                            String orderType = "Purchase";
                            double rentalDuration = 0.0; // Initialize rental duration to 0
                            if (orderTypeChoice == 2) {
                                orderType = "Rent";
                                System.out.print("Enter the rental duration in hours: ");
                                rentalDuration = scanner.nextDouble();
                            }
                            System.out.print("Enter the quantity: ");
                            int quantity = scanner.nextInt();
                            scanner.nextLine(); // Consume the newline character
                            orderName = "Order with " + quantity + " " + item.name + "(s)";
                            orderItems.add(new OrderItem(item, quantity));
                            // Set the status to DELIVERED if no more items are added
                            if (orderConfirmation.equals("n")) {
                                tracking.updateStatus(Tracking.Status.DELIVERED); // Set status to DELIVERED
                            }
                            priorityQueue.enqueue(new Order(orderName, 1, age, id, tracking));
                            System.out.println("Item added to the order.");
                            System.out.print("Do you want to add more items to your order? (y/n): ");
                            orderConfirmation = scanner.nextLine().toLowerCase();
                            // Calculate the item cost based on purchase or rental
                            double itemCost;
                            if (orderTypeChoice == 2) {
                                // Calculate the cost for renting
                                itemCost = rentalDuration * item.rentalPricePerHour;
                            } else {
                                // Calculate the cost for purchase
                                itemCost = quantity * item.purchasePrice;
                            }
                            totalCost += itemCost; // Add the item cost to the total cost
                        } else {
                            System.out.println("Invalid item name.");
                            orderConfirmation = "y"; // Repeat the loop if the item name is invalid
                        }
                    } while (orderConfirmation.equals("y"));
                    orderPriority = 2; // Default
                    if (age >= 75) {
                        orderPriority = 1;
                    }
                    Order newOrder = new Order(orderName, orderPriority, age, id, tracking);
                    priorityQueue.enqueue(newOrder);
                    System.out.println("Order placed.");
                    DecimalFormat df = new DecimalFormat("#.##");
                    System.out.println("\n\t\tBill Receipt:");
                    System.out.println("----------------------------------------");
                    System.out.println("\tOrder Name: " + newOrder.name);
                    System.out.println("\tTotal Cost: Rs." + df.format(totalCost));
                    System.out.println("\tPatient ID: " + newOrder.id);
                    System.out.println("\tDelivery Status: " + newOrder.tracking.getStatus());
                    System.out.println("----------------------------------------");
                    System.out.println("THANK YOU!!");
                    break;

                case 2:
                    System.out.println("Orders in Priority Queue:");
                    priorityQueue.display();
                    break;

                case 3:
                    System.out.print("Enter the order ID to track: ");
                    if (scanner.hasNextInt()) {
                        int orderToTrackID = scanner.nextInt();
                        scanner.nextLine();  // Consume the newline character
                        checkOrderStatus(priorityQueue, orderToTrackID);
                    } else {
                        System.out.println("Invalid input. Please enter a valid integer for the order ID.");
                        scanner.nextLine();  // Consume the invalid input
                    }
                    break;

                case 4:
                    System.out.println("Exiting the program.");
                    scanner.close();
                    System.exit(0);
                    break;

                default:
                    System.out.println("Invalid choice. Please select 1, 2, 3, or 4.");
                    break;
            }
        }
    }
}
