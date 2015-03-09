package NER;

public class Poptest {

	public static void main(String[] args) {
		PopulationFromFile data = null;
		try {
			data = new PopulationFromFile("sample.txt");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		data.printPersons();

	}

}
