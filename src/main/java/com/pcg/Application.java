package com.pcg;

import java.util.Scanner;

public class Application {
    public static void main(String[] args) {
        Scanner reader = new Scanner(System.in);
        System.out.println("Select a procedural content generation algorithm to run:\n\t1. Worley/Perlin\n\t2. Wave Function Collapse");
        int choice = reader.nextInt();
        while(choice != 1 && choice != 2) {
            System.out.println("Invalid choice. Please select a valid option.");
            choice = reader.nextInt();
        }

        switch (choice) {
            case 1:
                System.out.println("Running Worley/Perlin algorithm...");
                MarchingCubes.MarchingCubes();
                break;
            case 2:
                System.out.println("Running Wave Function Collapse algorithm...");
                // WFC main function goes here
                WFC.WaveFunctionCollapse();
                break;
        }
    }
}