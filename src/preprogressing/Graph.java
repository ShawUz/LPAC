package preprogressing;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;

public class Graph {
    
	public static int nodenum = 0;
	public static int edgenum = 0;
	public static ArrayList<Node> graph = new ArrayList<Node>();
	public static HashMap<Integer, Integer>id2newid = new HashMap<Integer, Integer>();
	
	public void read(String split, String graphpath) throws NumberFormatException, IOException {
		
		String encoding="GBK"; 
		File file = new File(graphpath);
		int newid = 0;
		 if (file.isFile() && file.exists()) { 
             InputStreamReader read = new InputStreamReader(new FileInputStream(file),encoding);
             BufferedReader bufferedReader = new BufferedReader(read);
                 String lineTxt = null;
                 System.out.println("read graph");
                 while ((lineTxt = bufferedReader.readLine()) != null) { 
                 	String[] mm = lineTxt.split(split);
                 	int m0 = Integer.parseInt(mm[0]);
                 	int m1 = Integer.parseInt(mm[1]);
                 	if (m0 == m1)
                 		continue;
                 	if (!id2newid.containsKey(m0)) {
                 		id2newid.put(m0, newid++);
                 	}
                 	if (!id2newid.containsKey(m1)) {
                 		id2newid.put(m1, newid++);
                 	}
                 	int id1 = id2newid.get(m0);
                 	int id2 = id2newid.get(m1);
                 	if (graph.size() > id1) {
                 		graph.get(id1).neighbors.add(id2);
                 	} else {
                 		Node node = new Node();
                 		node.neighbors.add(id2);
                 		graph.add(node);
                 	}
                 	if (graph.size() > id2) {
                 		graph.get(id2).neighbors.add(id1);
                 	} else {
                 		Node node = new Node();
                 		node.neighbors.add(id1);
                 		graph.add(node);
                 	}
                 	edgenum++;
                 } 
             read.close();
         }	
		 nodenum = newid;
		 System.out.println("read graph over");
		 for (int i = 0; i < nodenum; i++) {
			 Graph.graph.get(i).neighbors.add(i);
		 }
		 
	}
}
