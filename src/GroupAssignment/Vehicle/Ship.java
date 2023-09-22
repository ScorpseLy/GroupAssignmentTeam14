package GroupAssignment.Vehicle;
import GroupAssignment.Container.Container;
import GroupAssignment.FilePaths.FilePaths;
import GroupAssignment.ScreenDisplay.SystemAdmin;
import GroupAssignment.Vehicle.Vehicle;

import java.io.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Scanner;

public class Ship extends Vehicle {

    private static final String ContainerFilePath = FilePaths.ContainerFilePath;
    private double totalFuelConsumption;
    private double totalWeight = 0;

    private ArrayList<Container> carrier = new ArrayList<Container>(); //not an atrribute in the constructor

    private Container unloadedContainer = null;

    private double shipInitialFuelConsumption = 1; //Ship consumes 50 gallons per km
    public Ship(String id, String name, String type, double currentFuel, double fuelCapacity, double carryingCapacity, String currentPort) {
        super(id, name, type, currentFuel, fuelCapacity, carryingCapacity, currentPort);
    }

    public double getShipInitialFuelConsumption() {
        return shipInitialFuelConsumption;
    }
    @Override
    public double getTotalWeight() {
        return totalWeight;
    }
    @Override
    public void setTotalWeight(double totalWeight) {
        this.totalWeight = totalWeight;
    }

    @Override
    public ArrayList<Container> getCarrier() {
        return carrier;
    }

    public void setCarrier(ArrayList<Container> carrier) {
        this.carrier = carrier;
    }

    @Override
    public boolean loadContainer() {
        // If container totalWeight + container weight <= carryingCapacity
        Scanner scanner = new Scanner(System.in);
        System.out.println("Choose the container to load");
        System.out.print("Container ID: ");
        int ID = scanner.nextInt();
        scanner.nextLine();

        String searchID = Integer.toString(ID); // Replace with the ID you want to search for

        // Read data from the text file
        ArrayList<String> lines = new ArrayList<>();
        boolean firstLine = true; // Flag to track the first line (header)
        boolean found = false;

        try (BufferedReader reader = new BufferedReader(new FileReader(ContainerFilePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (firstLine) {
                    // Keep the first line (header)
                    lines.add(line);
                    firstLine = false;
                    continue;
                }

                String[] parts = line.split(",\\s+"); // Use regex to split by comma and optional spaces
                if (parts.length == 3) {
                    String id = parts[0].trim();
                    String containerType = parts[1];
                    double weight = Double.parseDouble(parts[2]);

                    Container container = new Container(id, containerType, weight);

                    // Check if the current container matches the search ID & the vehicle is still able to carry the next container
                    if (id.equals(searchID) && totalWeight + weight <= getCarryingCapacity()) {
                        carrier.add(container); // Add the found container to the list
                        found = true;
                        System.out.println("Container added ");
                        totalWeight += weight;
                        shipInitialFuelConsumption += Container.getShip_Container().get(container.getContainerType());
                    } else {
                        lines.add(line);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            return false; // Return false in case of an exception
        }

        if (found) {
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(ContainerFilePath))) {
                for (String line : lines) {
                    writer.write(line);
                    writer.newLine();
                }
            } catch (IOException e) {
                e.printStackTrace();
                return false; // Return false if writing fails
            }
        } else {
            System.out.println("Failed to add container");
            return false; // Return false if the container is not found or cannot be added
        }

        return found; // Return true if the container is successfully loaded
    }
    @Override
    public boolean unloadContainer() {
        // Telling the ID of the container to be removed
        Scanner scanner = new Scanner(System.in);
        System.out.print("Unloading container ID: ");
        int ID = scanner.nextInt();
        scanner.nextLine();

        String searchID = Integer.toString(ID);
        boolean successfullyUnloaded = false;

        // Check if the carrier is not empty
        if (!carrier.isEmpty()) {
            Iterator<Container> iterator = carrier.iterator();
            while (iterator.hasNext()) {
                Container container = iterator.next();
                if (container.getId().equals(searchID)) {
                    unloadedContainer = container;
                    totalWeight -= container.getWeight();
                    shipInitialFuelConsumption -= Container.getShip_Container().get(container.getContainerType());
                    iterator.remove(); // Remove the container
                    successfullyUnloaded = true;
                    break;
                }
            }

            // Check if a container was found and unloaded
            if (unloadedContainer != null) {
                if (Math.abs(totalWeight) < 1e-10) {
                    totalWeight = 0.0;
                }
                // Write the container information into container.txt
                try {
                    // Open the file in append mode by creating a FileWriter with the 'true' parameter
                    FileWriter writer = new FileWriter(ContainerFilePath, true);

                    // Write the container's attributes as plain text
                    writer.write("\n" + unloadedContainer.getId() + ", " + unloadedContainer.getContainerType() + ", " + unloadedContainer.getWeight());

                    // Close the FileWriter
                    writer.close();

                    System.out.println("Container unloaded.");
                } catch (IOException e) {
                    e.printStackTrace();
                    successfullyUnloaded = false;
                }
            } else {
                System.out.println("Container with ID " + searchID + " not found in the carrier.");
            }
        } else {
            System.out.println("There are no containers to be unloaded.");
        }

        return successfullyUnloaded;
    }
    @Override
    public boolean moveVehicle(Scanner scanner) {
        boolean ableToUnload = true;
        System.out.print("Move to port number: ");
        int destinationPort = scanner.nextInt();
        scanner.nextLine();

        for (Container container : carrier) {
            if (container.getWeight() + SystemAdmin.portList.get(destinationPort - 1).getCurrentStoring() > SystemAdmin.portList.get(destinationPort - 1).getStoringCapacity() && getCurrentFuel() <= 0) {
                ableToUnload = false;
            }
        }

        if(!ableToUnload){
            System.out.println("Cannot transport the vehicle to the designated port");
            return false;
        }
        else{
            setCurrentFuel(super.getCurrentFuel() - (SystemAdmin.calculateDistanceWithParameter(SystemAdmin.portList.get(extractPortNumber(getCurrentPort())-1).getP_ID(), SystemAdmin.portList.get(destinationPort - 1).getP_ID()) * shipInitialFuelConsumption));
            System.out.println("Vehicle moved to port "+destinationPort);
            setCurrentPort(SystemAdmin.portList.get(destinationPort - 1).getP_ID());
            return true;
        }
    }
    @Override
    public boolean refuelingVehicle(){
        setCurrentFuel(getFuelCapacity());
        return true;
    }

    @Override
    public String toString() {
        return "Ship{" +
                " id = '" + super.getId() + '\'' +
                ", name = '" + super.getName()+ '\'' +
                ", type= '" + "ship" + '\'' +
                ", currentFuel = " + super.getCurrentFuel() +
                ", fuelCapacity = " + super.getFuelCapacity() +
                ", carryingCapacity = " + super.getCarryingCapacity() +
                ", currentPort = " + super.getCurrentPort() +
                ", totalWeight = " + getTotalWeight() +
                '}';
    }
}
