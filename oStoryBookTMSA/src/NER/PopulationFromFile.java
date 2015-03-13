package NER;
import java.util.Vector;

import edu.stanford.nlp.ie.AbstractSequenceClassifier;
import edu.stanford.nlp.ie.crf.*;
import edu.stanford.nlp.io.IOUtils;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.sequences.DocumentReaderAndWriter;
import edu.stanford.nlp.util.Triple;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.github.irobson.jgenderize.GenderizeIoAPI;
import com.github.irobson.jgenderize.client.Genderize;
import com.github.irobson.jgenderize.model.NameGender;

import storybook.model.hbn.entity.Gender;
import storybook.model.hbn.entity.Person;

/*
 * PopulationFromFile accepts a file and you can optionally specify a classifer to use.
 * It reads and analyzes the file and returns a vector of unique persons found in the file.
 * Each of these persons is populated with a first and last name and a gender.
 */
public class PopulationFromFile {

	private String file = null;
	private String serializedClassifier = null; //Default Classifier
	private Vector<Person> persons = new Vector<Person>();
	private Genderize api = GenderizeIoAPI.create();
	
	public PopulationFromFile(){}
	
	public PopulationFromFile(String file) throws Exception {
		this.file = file;
		this.serializedClassifier = "classifiers/english.all.3class.distsim.crf.ser.gz";
		readfile();
	}
	
	public PopulationFromFile(String file, String classifier) throws Exception {
		this.file = file;
		this.serializedClassifier = classifier;
		readfile();
	}
	
	protected void readfile() throws Exception {
		AbstractSequenceClassifier<CoreLabel> classifier = CRFClassifier.getClassifier(serializedClassifier);
		String fileContents = IOUtils.slurpFile(file);
	      List<List<CoreLabel>> out = classifier.classify(fileContents);
	      List<Triple<String, Integer, Integer>> list = classifier.classifyToCharacterOffsets(fileContents);
	      for (Triple<String, Integer, Integer> item : list) {
	    	if(item.first().equals("PERSON")) {
	    		generatePerson(fileContents, item);
	    	}
	      }
	      removeDuplicates();
	}
	
	protected void generatePerson(String fileContents, Triple<String, Integer, Integer> item) {
		String fullname = fileContents.substring(item.second(), item.third());
		Pattern pattern = Pattern.compile("\\s");
		Matcher matcher = pattern.matcher(fullname);
		boolean found = matcher.find();
		String firstname = null;
		String lastname = null;
		
		if(found) { //If the string has spaces then parse it for first and last name
			String[] splited = fullname.split("\\s+");
			int size = splited.length;
			switch(size){
			case 1: //Just a first and last name
				firstname = splited[0];
				lastname = splited[1];
			case 2: //First, middle, and last name
				firstname = splited[0];
				lastname = splited[1];
			default: 
				/*
				 * Some number of names just take the first and last ones if there is no Jr. or Sr. involved
				 * if there is take the next to last part of the split string
				 */
				firstname = splited[0];
				if(hasTitle(fullname)){
					lastname = splited[splited.length - 2];
				} else {
					lastname = splited[splited.length - 1];
				}
			}	
		} else { //If it's just the first name then get the gender
			firstname = fullname;
		}
		
		Person person = new Person();
		person.setFirstname(firstname);
		person.setLastname(lastname);
		String abbr = null;
		if(lastname != null) {
			abbr = firstname.substring(0,2) + lastname.substring(0,2);
		} else {
			abbr = firstname.substring(0,2);
		}
		person.setAbbreviation(abbr);
		
		if(isPresent(firstname, lastname)) {
			//If this person is already there, don't bother calling genderize or adding the person
		} else {
			Gender g = new Gender(); //Storybook gender object
			g.setId(determineGender(firstname));
			person.setGender(g);
			persons.add(person);
		}
	}
	
	/*
	 * Checks the person vector to confirm that the person has not already been added
	 * -Checks full name
	 * -Checks gender
	 */
	
	private boolean isPresent(String firstname, String lastname) {
		for(int i = 0; i < persons.size(); i++) {
			if(firstname.equals(persons.get(i).getFirstname())) { //If the name is the same...
				return true;
			}
			if(firstname.equals(persons.get(i).getLastname())) {
				return true;
			}
		}
		return false;
	}
	
	/*
	 * Check each entry against all others, if it's:
	 * Not the same entry AND
	 * If the first name is the same as the last name of another entry OR
	 * If the first name is the same as the first name of another entry THEN
	 * mark it for removal 
	 */
	private void removeDuplicates() {
		Vector<Integer> removeSet = new Vector<Integer>();
		for(int i = 0; i < persons.size(); i++) {
			for(int j = 0; j < persons.size(); j++) {
				if(((i != j) && (persons.get(i).getLastname() == "") && (persons.get(i).getFirstname().equals(persons.get(j).getFirstname())) || (persons.get(i).getFirstname().equals(persons.get(j).getLastname())))){
					removeSet.add(i);
				}
			}
		}
		
		for(int k = removeSet.size(); k > 0; k--) { //Remove from the back forward
			persons.remove((int) removeSet.get(k-1));
		}
	}
	
	/*
	 * In oStoryBook a gender object's id attr of 1 means the gender is Male
	 * while 2 means that the gender is Female. This is why the function returns 1 or 2.
	 */
	private long determineGender(String firstname) {
		NameGender gender = null; //Jgenderize gender object 
		gender = api.getGender(firstname);
		//If we allowed for more genders, use a switch case here
		if(gender.isMale()) {
			return (long) 1;
		} else {
			return (long) 2;
		}
	}
	
	/*
	 * Checks specifically for Jr. and Sr. titles
	 * NER will not pick up the . in "Jr." or "Sr." taking a chance with Jr and Sr, but
	 * english names don't commonly have names that start with Jr or Sr...
	 * Note: Can be expanded to allow for more title checking
	 */
	private boolean hasTitle(String fullname) {
		if(fullname.contains("Jr") || fullname.contains("Sr")) {
			return true;
		} else {
			return false;
		}

	}
	
	public Vector<Person> getPopulation() {
		return persons;
	}
	
	public void setFile(String file){
		this.file = file;
	}
	
	public void setClassifer(String classifier) {
		this.serializedClassifier = classifier;
	}
	
	/*
	 * For testing
	 */
	public void printPersons() {
		for(int i =0; i < persons.size(); i++) {
			System.out.println("-------------");
			System.out.println("Firstname: " + persons.get(i).getFirstname());
			System.out.println("Lastname: " + persons.get(i).getLastname());
			System.out.println("Is male?: " + persons.get(i).getGender().isMale());
		}
	}
}
