package calauc;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import java.util.Stack;

import preprogressing.Graph;

public class Auc {
	
	public double ratio = 0.9; //训练集和测试集的比率
	public String cmtypath;
	public static ArrayList<int[]> smpin = new ArrayList<int[]>();
	public static ArrayList<int[]> smpout = new ArrayList<int[]>(); 
	int[] node2cmty = new int[Graph.nodenum]; 
	public int numebc = 0;
	
	
	public void removeEdge() { 
		Random ra = new Random();
		int choosen = 0;
		System.out.println("conduct the testing set");
		boolean flag = false;
		for (int i = 0; i < (1 - ratio) * numebc; i++) {
			choosen = ra.nextInt(Graph.nodenum);
			if (Graph.graph.get(choosen).neighbors.size() == 2) {
				i--;
				continue;
			}
				
			if (!Graph.graph.get(choosen).neighbors.isEmpty()) {
				Iterator<Integer> it = Graph.graph.get(choosen).neighbors.iterator();
				int node = -1;
				boolean exist = false;
				while (it.hasNext()) {
					node = it.next();
					if (node2cmty[choosen] != node2cmty[node]) {
						exist = true;
						break;
					}
				}
				if (exist) {
					int[] edge = null;
					if (choosen < node) {
						edge = new int[]{choosen, node};
					} else {
						edge = new int[]{node, choosen};
					}					
					Graph.graph.get(choosen).neighbors.remove(node);
					Graph.graph.get(node).neighbors.remove(choosen);
					if (judgeConnectivity()) {
						smpout.add(edge);
						flag = true;
					} else {
						Graph.graph.get(choosen).neighbors.add(node);
						Graph.graph.get(node).neighbors.add(choosen);
						i--;
					}
					
				} else {
					i--;
				}
			} else {
				i--;
			}
		}
		
		flag = false;
		for (int i = 0; i < (1 - ratio) * numebc; i++) {
			choosen = ra.nextInt(Graph.nodenum);
			if (Graph.graph.get(choosen).neighbors.size() == 2) {
				i--;
				continue;
			}
			if (!Graph.graph.get(choosen).neighbors.isEmpty()) {
				Iterator<Integer> it = Graph.graph.get(choosen).neighbors.iterator();
				int node = -1;
				boolean exist = false;
				while (it.hasNext()) {
					node = it.next();
					if (node == choosen)
						continue;
					if (node2cmty[choosen] == node2cmty[node]) {
						exist = true;
						break;
					}
				}
				if (exist) {
					int[] edge = null;
					if (choosen < node) {
						edge = new int[]{choosen, node};
					} else {
						edge = new int[]{node, choosen};
					}	
					Graph.graph.get(choosen).neighbors.remove(node);
					Graph.graph.get(node).neighbors.remove(choosen);
					if (judgeConnectivity()) {
						smpin.add(edge);
						flag = true;
					} else {
						Graph.graph.get(choosen).neighbors.add(node);
						Graph.graph.get(node).neighbors.add(choosen);
						i--;
					}
				} else {
					i--;
				}
			} else {
				i--;
			}
		}
		System.out.println("finish conducting the testing set");
    }
	
	public void getEbc(String split) throws IOException {
		String encoding="GBK"; 
		File file = new File(cmtypath);
		HashSet<Integer> overlapnode = new HashSet<Integer>();
		
		for(int i = 0; i < node2cmty.length; i++) {
			node2cmty[i] = -1;
		}
		int count = 0, notincmty = 0; 
		int[] overlap = new int[Graph.nodenum];
		if (file.isFile() && file.exists()) { 
             InputStreamReader read = new  InputStreamReader(new FileInputStream(file),encoding);
             BufferedReader bufferedReader = new BufferedReader(read);
                 String lineTxt = null;
                 int commid = 0;
                 while ((lineTxt = bufferedReader.readLine()) != null) {
                 	String[] mm = lineTxt.split(split);
                 	for (int i = 0; i < mm.length; i++) {
                 		if (node2cmty[Integer.valueOf(mm[i])] != -1 && overlap[Integer.valueOf(mm[i])] == 0) {
                 			count++;
                 			overlap[Integer.valueOf(mm[i])] = 1;
                 			overlapnode.add(Integer.valueOf(mm[i]));
         
                 		}
                 		node2cmty[Integer.valueOf(mm[i])] = commid;
                 	}
                 	commid++;
                 }
            read.close();
		}
		for (int i = 0; i < Graph.nodenum; i++) {  
			if (overlapnode.contains(i)) 
				continue;
			if (node2cmty[i] == -1)
				notincmty++;
			Iterator<Integer> it = Graph.graph.get(i).neighbors.iterator();
			while (it.hasNext()) {
				int j = it.next();				
				if (node2cmty[i] != node2cmty[j]) {
					if (i < j) {
						numebc++;
					}					
				}
			}
		}
	}
	
	public boolean judgeConnectivity() { 
		boolean[] visited = new boolean[Graph.nodenum];
		Stack<Integer> s = new Stack<Integer>();
		Random ra = new Random();
		int start = ra.nextInt(Graph.nodenum - 1);
		visited[start] = true;
		s.push(start);
		int visit = 1;
		while (!s.isEmpty()) { 
			int k = s.peek();
			boolean needpop = true;
			for(Iterator<Integer> it = Graph.graph.get(k).neighbors.iterator(); it.hasNext();) {
				int neighbor = it.next();
				if (visited[neighbor] == false) {
					s.push(neighbor);
					visited[neighbor] = true;
					visit++;
					needpop = false;
					break;
				}
			}
			if (needpop) {
				s.pop();
			}
		}
		return visit == Graph.nodenum;
	}
	
	public void writeSamplesForFinalPrune(String inpath, String outpath, double epsilon, String dsname) throws IOException {
		File in = new File(inpath);
		HashMap<Integer, HashMap<Integer, Float>> inhm = new HashMap<Integer, HashMap<Integer, Float>>();
		HashMap<Integer, HashMap<Integer, Float>> outhm = new HashMap<Integer, HashMap<Integer, Float>>();
		for (int i = 0; i < smpin.size(); i++) {
			if (inhm.containsKey(smpin.get(i)[0])) {
				inhm.get(smpin.get(i)[0]).put(smpin.get(i)[1], 0f);
			} else {
				HashMap<Integer, Float> hm = new HashMap<Integer, Float>();
				hm.put(smpin.get(i)[1], 0f);
				inhm.put(smpin.get(i)[0], hm);
			}
		}
		for (int i = 0; i < smpout.size(); i++) {
			if (outhm.containsKey(smpout.get(i)[0])) {
				outhm.get(smpout.get(i)[0]).put(smpout.get(i)[1], 0f);
			} else {
				HashMap<Integer, Float> hm = new HashMap<Integer, Float>();
				hm.put(smpout.get(i)[1], 0f);
				outhm.put(smpout.get(i)[0], hm);
			}
		}
		if (in.isDirectory()) {
			String[] filelist = in.list();
			String encoding="GBK";
			for (int i = 0; i < filelist.length; i++) {
				File readfile = new File(inpath + "\\" + filelist[i]);
				InputStreamReader read = new InputStreamReader(new FileInputStream(readfile),encoding);//考虑到编码格式
	            BufferedReader bufferedReader = new BufferedReader(read);
	            String lineTxt = null;
	            while ((lineTxt = bufferedReader.readLine()) != null) {
	            	String[] split = lineTxt.split(" ");
	            	int id1 = Integer.valueOf(split[0]), id2 = Integer.valueOf(split[1]);
	            	float sim = Float.valueOf(split[2]);
	            	if (inhm.containsKey(id1) && inhm.get(id1).containsKey(id2)) {
	            		if (inhm.get(id1).get(id2) < sim) {
	            			inhm.get(id1).put(id2, sim);
	            		}
	            	}
	            	if (outhm.containsKey(id1) && outhm.get(id1).containsKey(id2)) {
	            		if (outhm.get(id1).get(id2) < sim) {
	            			outhm.get(id1).put(id2, sim);
	            		}
	            	}
	            }
	            read.close();
			}
			
		}
		
		File out = new File(outpath);
		BufferedWriter writer = null;
		writer = new BufferedWriter(new FileWriter(out));
		String EOL = System.getProperty("line.separator");
		int countin = 0, countout = 0, uniform = 0;
		for (Map.Entry<Integer, HashMap<Integer, Float>> e1 : inhm.entrySet()) {
			for (Map.Entry<Integer, Float> e2 : e1.getValue().entrySet()) {
				if (e2.getValue() > 0)
					countin++;
			}
		}
		for (Map.Entry<Integer, HashMap<Integer, Float>> e1 : outhm.entrySet()) {
			for (Map.Entry<Integer, Float> e2 : e1.getValue().entrySet()) {
				if (e2.getValue() > 0)
					countout++;
			}
		}
		uniform = countin > countout ? countout : countin;
		countin = 0; countout = 0;
		for (Map.Entry<Integer, HashMap<Integer, Float>> e1 : inhm.entrySet()) {
			for (Map.Entry<Integer, Float> e2 : e1.getValue().entrySet()) {
				if (countin == uniform)
					break;
				if (e2.getValue() > 0) {
					writer.write(singleValue(e2.getValue(), (float)epsilon) + " " + 0 + EOL);
					countin++;
				}
			}
		}
		for (Map.Entry<Integer, HashMap<Integer, Float>> e1 : outhm.entrySet()) {
			for (Map.Entry<Integer, Float> e2 : e1.getValue().entrySet()) {
				if (countout == uniform)
					break;
				if (e2.getValue() > 0) {
					writer.write(singleValue(e2.getValue(), (float)epsilon) + " " + 1 + EOL);
					countout++;
				}
			}
		}
		writer.flush();
		writer.close();
		
		File path = new File("./forNode2vec");
		if (!path.exists()) {
			path.mkdirs();
		}
		path = new File("./forNode2vec/" + dsname);
		if (!path.exists()) {
			path.mkdirs();
		}
		File fileout = new File("./forNode2vec/" + dsname + "/" + dsname + ".txt");
		writer = null;
		writer = new BufferedWriter(new FileWriter(fileout));
		for (int i = 0; i < Graph.nodenum; i++) {
			Iterator<Integer> it = Graph.graph.get(i).neighbors.iterator();
			while (it.hasNext()) {
				int nbr = it.next();
				if (nbr == i)
					continue;
				writer.write(i + " " + nbr + EOL);
			}
		}
		writer.flush();
		writer.close();
		
		fileout = new File("./forNode2vec/" + dsname + "/" + dsname + "_smpin.txt");
		writer = new BufferedWriter(new FileWriter(fileout));
		countin = 0; countout = 0;
		for (Map.Entry<Integer, HashMap<Integer, Float>> e1 : inhm.entrySet()) {
			for (Map.Entry<Integer, Float> e2 : e1.getValue().entrySet()) {
				if (countin == uniform)
					break;
				writer.write(e1.getKey() + " " + e2.getKey() + " " + 0 + EOL);
				countin++;
			}
		}
		
		writer.flush();
		writer.close();
		
		fileout = new File("./forNode2vec/" + dsname + "/" + dsname + "_smpout.txt");
		writer = new BufferedWriter(new FileWriter(fileout));
		countin = 0; countout = 0;
		for (Map.Entry<Integer, HashMap<Integer, Float>> e1 : outhm.entrySet()) {
			for (Map.Entry<Integer, Float> e2 : e1.getValue().entrySet()) {
				if (countout == uniform)
					break;
				writer.write(e1.getKey() + " " + e2.getKey() + " " + 1 + EOL);
				countout++;
			}
		}
		writer.flush();
		writer.close();
	}
	

	
	public float singleValue(float sim, float epsilon) {
		float score = sim;
		if (score > epsilon)
			score = 2 * epsilon -score;
		return score;
	}
	
}
