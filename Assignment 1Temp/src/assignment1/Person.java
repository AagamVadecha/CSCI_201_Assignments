package assignment1;

import java.util.ArrayList;

public class Person implements Comparable<Person> {
	String _fname;
	String _lname;
	String _email;
	int _age;
	boolean _nearCampus;
	ArrayList<String> _notes;
	public Person(String a, String b, String c, int d, boolean f, ArrayList<String> g) {
		_fname = a;
		_lname = b;
		_email = c;
		_age = d;
		_nearCampus = f;
		_notes = g;
	}
	
	public void addNote(String note)
	{
		_notes.add(note);
	}
	
	public String toString(){
		String y = "Name: " + _fname + " " + _lname + "\nEmail: " + _email + "\n Age: " + _age + "\nNear campus: ";
		if(_nearCampus)
			y+= "Yes";
		else
			y+= "No";
		y+= "\nNotes: ";
		for(int x=0; x < _notes.size(); x++)
		{
			
			y += _notes.get(x); 
			if(x!=_notes.size()-1)
				y+= ", ";	
			
		}
		return y + "\n";
		
	}
	
	public String toStringFile() {
		String y = _fname + "," + _lname + "," + _email + "," + _age + "," + Boolean.toString(_nearCampus);
		for(int x=0; x<_notes.size(); x++)
		{
			y+="," + _notes	.get(x);
		}
		return y;
	}
	
	@Override
	public int compareTo(Person o) {
		if(_lname.equals(o._lname))
		{
			return _fname.compareTo(o._fname);
		}
		else
			return _lname.compareTo(o._lname);
	}
}



