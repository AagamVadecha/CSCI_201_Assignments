package assignment1;

import java.util.ArrayList;

public class Person implements Comparable<Person> {
	private String _fname;
	private String _lname;
	private String _email;
	private int _age;
	private boolean _nearCampus;
	private String _notes;
	public Person(String a, String b, String c, int d, boolean f, String g) {
		_fname = a;
		_lname = b;
		_email = c;
		_age = d;
		_nearCampus = f;
		_notes = g;
	}
	
	public void addNote(String note)
	{
		_notes = _notes + "," + note;
	}

	@Override
	public String toString(){
		StringBuilder y = new StringBuilder("Name: " + _fname + " " + _lname + "\nEmail: " + _email + "\nAge: " + _age + "\nNear campus: ");
		if(_nearCampus)
			y.append("Yes");
		else
			y.append("No");
		y.append("\nNotes: " + _notes);
		return y.toString() + "\n";
	}
	
	public String toStringFile() {
		StringBuilder y = new StringBuilder(_fname + "," + _lname + "," + _email + "," + _age + "," + Boolean.toString(_nearCampus) + "," + _notes);
		return y.toString();
	}

	public String get_fname() {
		return _fname;
	}

	public String get_lname() {
		return _lname;
	}

	public String get_email() {
		return _email;
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



