package assignment1;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Scanner;

public class Assignment1 {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(new InputStreamReader(System.in));
        Scanner scannerFile = null;
        File file;
        boolean properFile = false;
        ArrayList<Person> people = null;

        do {
            System.out.println("What is the name of the contacts file?");
            String fileName = scanner.nextLine();
            file = new File(fileName);
            properFile = true;
            if(!file.exists())
            {
                System.out.println("The file " + fileName + " could not be found. \n");
            }
            else {
                try {
                    scannerFile = new Scanner(file);
                    people =  new ArrayList<Person>();
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                while(scannerFile.hasNext())
                {
                    Person toAdd = null;
                    try {
                        toAdd = parseLine(scannerFile.nextLine());
                    } catch (Exception e) {
                        System.out.println("This file " + fileName + " is not formatted properly. \n");
                        System.out.println(e.getMessage());
                        properFile = false;
                        break;
                    }
                    people.add(toAdd);
                }
                if(people.size() == 0)
                    properFile = false;
            }
        }while(!file.exists() || !properFile);

        assert people != null;
        Collections.sort(people);
        boolean inLine = true;
        do{
            printMenu();
            String command = scanner.nextLine();
            int commandNum;
            try{
                commandNum = Integer.parseInt(command);
                switch(commandNum){
                    case 1:
                        System.out.println("Enter the contact's last name.");
                        boolean notFoundPerson = false;
                            try {
                                findPerson(scanner.nextLine(),people);
                            }
                            catch(Exception e) {
                                System.out.println(e.getMessage());
                            }
                        break;
                    case 2:
                        System.out.println("What is the first name of your new contact");
                        String fname = scanner.nextLine();
                        System.out.println("What is the last name of your new contact?");
                        String lname = scanner.nextLine();
                        boolean valid = true;
                        String email = null;
                        boolean nearCampus = false;
                        int age = -1;
                        String notes;
                        StringBuilder temp = new StringBuilder("");
                        do{
                            System.out.println("What is the email of your new contact?");
                            try{
                                email = parseEmail(scanner.nextLine());
                                valid = true;
                            }
                            catch(Exception e){
                                System.out.println("This is not a valid email. An email must have the formatting: xxxx@yyyy.com (or .net or .edu)");
                                valid = false;
                            }
                        }while(!valid);
                        do {
                            try {
                                System.out.println("What is the age of your new contact?");
                                age = Integer.parseInt(scanner.nextLine());
                                valid = true;
                            } catch (Exception e) {
                                valid = false;
                                System.out.println("This is an invalid integer. Please try again");
                            }
                        }while(!valid);
                        do{
                            try{
                                System.out.println("Does your new contact live near campus? Yes or No?");
                                nearCampus = parseBool(scanner.nextLine());
                                valid = true;
                            }
                            catch(Exception e){
                                valid = false;
                                System.out.println("This is not a yes or a no. Please try again");
                            }
                        }while(!valid);
                        do{
                            System.out.println("Add a note about your new contact.");
                            temp.append(scanner.nextLine() + ",");
                            boolean answeredProperly = true;
                            do {
                                System.out.println("Do you want to add another note?");
                                String bool = scanner.nextLine();
                                answeredProperly = true;
                                if (bool.equalsIgnoreCase("Yes"))
                                    valid = false;
                                else if (bool.equalsIgnoreCase("No"))
                                    valid = true;
                                else {
                                    answeredProperly = false;
                                    System.out.println("That's not a yes nor a no. Please try again.");
                                }
                            }while(!answeredProperly);
                        }
                        while(!valid);
                        temp.deleteCharAt(temp.length()-1);
                        notes = temp.toString();
                        Person toAdd = new Person(fname,lname, email, age, nearCampus, notes);
                        people.add(toAdd);
                        Collections.sort(people);
                        System.out.println(fname + " " + lname + " has been added to your contact list.");
                        break;
                    case 3:
                        System.out.println("Please give me the email of the person you want to delete.");
                        try{
                            deletePerson(scanner.nextLine(), people);
                        }
                        catch(Exception e){
                            System.out.println(e.getMessage());
                        }
                        break;
                    case 4:
                        System.out.println("Please give me the name of the file you want to write to.");
                        printFile(scanner.nextLine(), people);
                        break;
                    case 5:
                        inLine = false;
                        break;
                    default:
                        System.out.println("This is not a valid menu option. Please try again.");
                        break;
                }
            }
            catch(Exception e){
                if(command.equals("EXIT"))
                    inLine = false;
                else
                    System.out.println("Please choose a valid menu option.");
            }
        }
        while(inLine);
        System.out.println("Thank you for using my contacts program. Goodbye.");
        scanner.close();
    }

    public static Person parseLine(String line) throws Exception {
        String[] values = line.split(",");
        if(values.length < 6)
        {
            System.out.println("There are not enough parameters on line: \n'" + line + "'.");
            throw new Exception();
        }
        else
        {
            for(int x=0; x< values.length; x++)
                values[x] = values[x].trim();
            String _fname = values[0];
            String _lname = values[1];
            String _email;
            int _age;
            boolean _isNearCampus;
            StringBuilder y = new StringBuilder();
            try {
                _age = Integer.parseInt(values[3]);
            }
            catch(Exception e){
                throw new Exception("The parameter " + values[3] + " cannot be parsed as an age.");
            }
            try {
                _email = parseEmail(values[2]);
                if(values[4].equalsIgnoreCase("false"))
                    _isNearCampus = false;
                else if(values[4].equalsIgnoreCase("true"))
                    _isNearCampus = true;
                else
                    throw new Exception("The parameter " + values[4] + " cannot be parsed as whether or not the student is on campus.");
            }
            catch(Exception e) {
                throw new Exception(e.getMessage());
            }
            for(int x=5; x< values.length; x++)
            {
                y.append(values[x]);
                if(x!=values.length-1)
                    y.append(", ");
            }
            return new Person(_fname, _lname, _email, _age, _isNearCampus, y.toString());
        }
    }

    public static String parseEmail(String email) throws Exception
    {
        if(email.matches("^(.+)@(.+)(.com|.edu|.net)$"))
            return email;
        throw new Exception("The parameter " + email +" cannot be parsed as an e-mail.");
    }

    public static boolean parseBool(String bool) throws Exception
    {
        if(bool.equalsIgnoreCase("Yes"))
            return true;
        else if(bool.equalsIgnoreCase("No"))
            return false;
        throw new Exception("The parameter " + bool + " cannot be parsed as whether or not the person is near campus.");
    }
    public static void printMenu() {
        System.out.println("1) Contact lookup \n2) Add contact \n3) Delete contact \n4) Print to a file \n5) Exit\n");
        System.out.println("What option would you like to select?");
    }

    public static void printFile(String fileName, ArrayList<Person> people)
    {
        try{
            PrintWriter out = new PrintWriter(fileName);
            for(int x=0; x< people.size(); x++)
            {
                out.println(people.get(x).toStringFile());
            }
            out.close();
        }
        catch(Exception e){
            System.out.println("An error occurred, please try again.");
            return;
        }
        System.out.println("Successfully printed all your contacts to " + fileName + "\n");
    }

    public static void findPerson(String person, ArrayList<Person> people) throws Exception{
        boolean foundSomeone = false;
        for(int x=0; x<people.size(); x++) {
            if(people.get(x).get_lname().equalsIgnoreCase(person)){
                System.out.println(people.get(x).toString());
                foundSomeone = true;
            }
        }
        if(!foundSomeone)
            throw new Exception("There is no one with the last name " + person + " in your contact book.");
    }

    public static ArrayList<Person> deletePerson(String email, ArrayList<Person> people) throws Exception{
        boolean deletedPerson = false;
        for(int x=0; x<people.size(); x++) {
            if (people.get(x).get_email().equals(email)) {
                people.remove(x);
                deletedPerson = true;
            }
        }
        if(!deletedPerson)
            throw new Exception(email + " doesn't exist in your contacts list.");
        return people;
    }

}
