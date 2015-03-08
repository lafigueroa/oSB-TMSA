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

import storybook.model.hbn.entity.Gender;
import storybook.model.hbn.entity.Person;

public class PopulationFromFile {

	private String file;
	private String serializedClassifier = "classifiers/english.all.3class.distsim.crf.ser.gz"; //Default Classifier
	private Vector<Person> persons;
	
	public PopulationFromFile(String file) throws Exception {
		this.file = file;
		readfile();
	}
	
	protected void readfile() throws Exception {
		AbstractSequenceClassifier<CoreLabel> classifier = CRFClassifier.getClassifier(serializedClassifier);
		String fileContents = IOUtils.slurpFile(file);
	      List<List<CoreLabel>> out = classifier.classify(fileContents);
	      List<Triple<String, Integer, Integer>> list = classifier.classifyToCharacterOffsets(fileContents);
	      for (Triple<String, Integer, Integer> item : list) {
	        generatePerson(fileContents, item);
	      }
	}
	
	protected void generatePerson(String fileContents, Triple<String, Integer, Integer> item) {
		String fullname = fileContents.substring(item.second(), item.third());
		Pattern pattern = Pattern.compile("\\s");
		Matcher matcher = pattern.matcher(fullname);
		boolean found = matcher.find();
		String firstname = null;
		String lastname = null;
		String gender = null;
		if(found) { //If the string has spaces then parse it for first and last name
			String[] splited = fullname.split("\\s+");
			int size = splited.length;
			switch(size){
			case 2: //Just a first and last name
				firstname = splited[0];
				lastname = splited[1];
				
				//Call genderize on first name
				//Return gender
			case 3: //First, middle, and last name
				firstname = splited[0];
				lastname = splited[2];
				//Call genderize on first name
				//return gender
			default:
				firstname = splited[0];
				lastname = splited[splited.length - 1];
				//Just take first name and the last one
				//return gender
			}	
		} else { //If it's just the first name then get the gender
			firstname = fullname;
			//Call generize on the first name
			//get gender
		}
		
		Person person = new Person();
		Gender g = new Gender();
		long maleId = 1; //Had to look this up in the gender class
		long femaleId = 2;
		if(gender == "male"){
			g.setId(maleId);
		} else {
			g.setId(femaleId);
		}
		person.setFirstname(firstname);
		person.setLastname(lastname);
		person.setGender(g);
		persons.add(person);
	}
	
	public Vector<Person> getPopulation() {
		return persons;
	}
	
	public void setFile(String file){
		this.file = file;
	}
}
