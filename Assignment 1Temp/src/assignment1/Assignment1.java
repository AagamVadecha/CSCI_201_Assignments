package assignment1;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Scanner;

public class Assignment1 {
	public static void main() {
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
				System.out.println("The file" + fileName + " could not be found. \n");
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
			}
		}while(!file.exists() && !properFile);
		
		
		
		
		scanner.close();
	}
	
	public static Person parseLine(String line) throws Exception
	{
		String[] values = line.split(",");
		if(values.length < 6)
		{
			System.out.println("There are not enough parameters on line: \n'" + line + "'.");
			throw new Exception();
		}
		else
		{
			String _fname = values[0];
			String _lname = values[1];
			String _email;
			int _age;
			boolean _isNearCampus;
			String y = "";
			try {
				_age = Integer.parseInt(values[3]);
			}
			catch(Exception e){
				throw new Exception("The parameter " + values[3] + " cannot be parsed as an age.");
			}
			try {
				_email = parseEmail(values[2]);
				_isNearCampus = parseBool(values[4]);
			}
			catch(Exception e) {
				throw new Exception(e.getMessage());
			}
			for(int x=5; x< values.length; x++)
			{
				y += values[x]; 
				if(x!=values.length-1)
					y+= ", ";	
			}
			return new Person(_fname, _lname, _email, _age, _isNearCampus, y);
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
		if(bool.equalsIgnoreCase("Yes") || bool.equalsIgnoreCase("true"))
			return true;
		else if(bool.equalsIgnoreCase("No") || bool.equalsIgnoreCase("false"))
			return false;
		throw new Exception("The parameter " + bool + " cannot be parsed as whether or not the person is near campus.");
	}
	public void printMenu() {
		System.out.println("1) Contact lookup \n 2) Add contact \n 3) Delete contact \n 4) Print to a file \n 5) Exit");
	}
	
}
