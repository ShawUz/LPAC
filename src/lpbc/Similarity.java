package lpbc;

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
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import calauc.Auc;
import preprogressing.Graph;

public class Similarity {
		
	public void calSimilarityForThread(int hop, String filepath, int threadid, Set<Integer> syncore) throws IOException {
		int count = 0;
		File fileout = new File(filepath + "/" + threadid + ".txt");
	    BufferedWriter writer = null;
		writer = new BufferedWriter(new FileWriter(fileout));
		String EOL = System.getProperty("line.separator");
		HashMap<Integer, HashMap<Integer, Float>> sims = null; 
		HashMap<Integer, ArrayList<Integer>> records = null;  
	    HashMap<Integer, HashSet<Integer>> reaches = null; 
	    boolean flag = false; 
	    int nextcore = -1, nextremainhop = -1;
	    ArrayList<Integer> lowlevel = null;
	    while (true) {
	    	if (!flag) {  
	    	    sims = new HashMap<Integer, HashMap<Integer, Float>>();
		    	int l1 = 0;
				synchronized(syncore) {
					Iterator<Integer> itcore = syncore.iterator();
					l1 = itcore.next();
					syncore.remove(l1);
				}			

				int[] level = new int[Graph.nodenum];   
				for (int i = 0; i < Graph.nodenum; i++) {
					level[i] = -1;
				}
				Queue<Integer> q = new LinkedList<Integer>();				
			    q.add(l1);
			    level[l1] = 0;
			    sims.put(l1, new HashMap<Integer, Float>());
			    sims.get(l1).put(l1, 1f);
			    while(!q.isEmpty()) {
			    	int explore = q.poll();
			    	if (level[explore] == hop)
			    		break;
			    	for (Iterator<Integer> it = Graph.graph.get(explore).neighbors.iterator(); it.hasNext(); ) {
			    		int neighbor = it.next();
			    		if (level[neighbor] == -1) {
				    		q.add(neighbor);
				    		sims.put(neighbor, new HashMap<Integer, Float>());
				    		sims.get(neighbor).put(neighbor, 1f);
				    		level[neighbor] = level[explore] + 1;
			    		}
			    	}
			    }
			    
			    synchronized(syncore){ 
			    	if (!syncore.isEmpty()) {
			    		Iterator<Integer> itcore = syncore.iterator();
			    		int minlevel = Integer.MAX_VALUE;
			    		while (itcore.hasNext()) {
			    			int temp = itcore.next();
			    			if (level[temp] != -1 && level[temp] != hop && minlevel > level[temp] ) {
			    				minlevel = level[temp];
			    				nextcore = temp;
			    				nextremainhop = level[temp];
			    				flag = true;
			    			}
			    		}
			    		if (flag)
			    			syncore.remove(nextcore);
			    	}
			    }
			    
			    for (int i = 0; i < level.length; i++) {
			    	if (level[i] == hop) {
			    		sims.put(i, new HashMap<Integer, Float>());
			    		sims.get(i).put(i, 1f);
			    	}
			    }
			    records = new HashMap<Integer, ArrayList<Integer>>();  
			    reaches = new HashMap<Integer, HashSet<Integer>>(); 
			    ArrayList<ArrayList<Integer>> nodes = new ArrayList<ArrayList<Integer>>();
			    for (int i = 0; i <= hop; i++) {
			    	ArrayList<Integer> hs = new ArrayList<Integer>();
			    	nodes.add(hs);
			    }
			    for (int i = 0; i < Graph.nodenum; i++) {  
			    	if (level[i] != -1) {
			    		nodes.get(level[i]).add(i);		
			    		if (level[i] != hop) {
					    	ArrayList<Integer> al = new ArrayList<Integer>();
					    	records.put(i, al);
					    	HashSet<Integer> hs = new HashSet<Integer>();
					    	reaches.put(i, hs);
			    		} else {
			    			HashSet<Integer> hs = new HashSet<Integer>();
			    			hs.add(i);
					    	reaches.put(i, hs);
			    		}
			    	}	    		    	
			    }
			    	  
			    calToLevel(nodes, level, sims, records, reaches, hop, 0);
			   
			    if (count % 30 == 0){
					System.gc();
			    }
			    count++;
			    		   
			    records=null;
			    reaches=null;			    
	    	} else {    
	    		flag = false;
	    		int[] level1 = new int[Graph.nodenum];  
				for (int i = 0; i < Graph.nodenum; i++) {
					level1[i] = -1;
				}
				Queue<Integer> q = new LinkedList<Integer>();				
			    q.add(nextcore);
			    level1[nextcore] = 0;
			    
			    lowlevel = new ArrayList<Integer>(); 
			    Iterator<Map.Entry<Integer, HashMap<Integer, Float>>> iter1 = sims.entrySet().iterator();
			    while (iter1.hasNext()) {
			    	Map.Entry<Integer, HashMap<Integer, Float>> entry1 = iter1.next();
			    	if (level1[entry1.getKey()] != 0) {
			    		iter1.remove();
			    	} else {			    		
			    		Iterator<Map.Entry<Integer, Float>> iter2 = entry1.getValue().entrySet().iterator();
			    		while (iter2.hasNext()) {
				    		int key2 = iter2.next().getKey();
				    		if (level1[key2] == -1) {
				    			iter2.remove();
				    		} else {
				    			if (key2 != nextcore) {
				    				lowlevel.add(key2);
				    			}
				    		}
			    		}
			    	}
			    }
			    
			    while(!q.isEmpty()) {
			    	int explore = q.poll();
			    	if (level1[explore] == hop)
			    		break;
			    	for (Iterator<Integer> it = Graph.graph.get(explore).neighbors.iterator(); it.hasNext(); ) {
			    		int neighbor = it.next();
			    		if (level1[neighbor] == -1) {
				    		q.add(neighbor);
				    		if (!sims.containsKey(neighbor)) {
				    			sims.put(neighbor, new HashMap<Integer, Float>());
				    			sims.get(neighbor).put(neighbor, 1f);
				    		}
				    		level1[neighbor] = level1[explore] + 1;
			    		}
			    	}
			    }
			    records = new HashMap<Integer, ArrayList<Integer>>();  
			    reaches = new HashMap<Integer, HashSet<Integer>>();
			    ArrayList<ArrayList<Integer>> nodes = new ArrayList<ArrayList<Integer>>();
			    for (int i = 0; i <= hop; i++) {
			    	ArrayList<Integer> hs = new ArrayList<Integer>();
			    	nodes.add(hs);
			    }
			    for (int i = 0; i < Graph.nodenum; i++) {   
			    	if (level1[i] != -1) {
			    		nodes.get(level1[i]).add(i);		
			    		if (level1[i] != hop) {
					    	ArrayList<Integer> al = new ArrayList<Integer>();
					    	records.put(i, al);
					    	HashSet<Integer> hs = new HashSet<Integer>();
					    	reaches.put(i, hs);
			    		} else {
			    			HashSet<Integer> hs = new HashSet<Integer>();
			    			hs.add(i);
					    	reaches.put(i, hs);
			    		}
			    	}	    		    	
			    }
			    
			   
			 	calToLevel(nodes, level1, sims, records, reaches, hop, 1);
			 		for (int low : lowlevel) {
			 			Iterator<Integer> iterreaches = reaches.get(low).iterator();
			 			while (iterreaches.hasNext()) {
			 				int reach = iterreaches.next();
			 				updateSimsForFinalPrune(nextcore, reach, sims.get(nextcore).get(low) * sims.get(low).get(reach), sims);
			 			}
			 		}
			 	synchronized(syncore){ //choose the next core node
			    	if (!syncore.isEmpty()) {
			    		Iterator<Integer> itcore = syncore.iterator();
			    		int minlevel = Integer.MAX_VALUE;
			    		while (itcore.hasNext()) {
			    			int temp = itcore.next();
			    			if (level1[temp] != -1 && level1[temp] != hop && minlevel > level1[temp]) {
			    				minlevel = level1[temp];
			    				nextcore = temp;
			    				nextremainhop = level1[temp];
			    				flag = true;
			    			}
			    		}
			    		if (flag)
			    			syncore.remove(nextcore);
			    	}
			    }
			 	records=null;
			    reaches=null;	
			    lowlevel = null;
			    count++;			   
	    	}
	    	 if (count % 30 == 0) {
					System.gc();
			    }
	    	 int id1 = 0, id2 = 0;
			    // link prediction across communities
			    for (int i = 0; i < Auc.smpin.size(); i++) { 
			    	id1 = Auc.smpin.get(i)[0];
			    	id2 = Auc.smpin.get(i)[1];
			    	if (sims.containsKey(id1)) {
			    		if (sims.get(id1).containsKey(id2)) {
			    			writer.write(id1 + " " + id2 + " " + sims.get(id1).get(id2) + " 0" + EOL);
			    		}
			    	}
			    }
			    for (int i = 0; i < Auc.smpout.size(); i++) {
			    	id1 = Auc.smpout.get(i)[0];
			    	id2 = Auc.smpout.get(i)[1];
			    	if (sims.containsKey(id1)) {
			    		if (sims.get(id1).containsKey(id2)) {
			    			writer.write(id1 + " " + id2 + " " + sims.get(id1).get(id2) + " 1" + EOL);
			    		}
			    	}
			    }
			    synchronized(syncore) {
					if (syncore.isEmpty())
						break;
				}
	    }
		writer.close();
	}
	
	public void calToLevel(ArrayList<ArrayList<Integer>> nodes, int[] level, HashMap<Integer, HashMap<Integer, Float>> sims,
    		HashMap<Integer, ArrayList<Integer>> records, HashMap<Integer, HashSet<Integer>> reaches, int hop, int highestlevel) {
		 for (int i = nodes.size() - 2; i >= highestlevel; i--) {
		    	HashMap<Integer, HashSet<Integer>> directreach = new HashMap<Integer, HashSet<Integer>>();
		    	for (int k = 0; k < 2; k++) {
		    		if (k == 0) {
				    	Iterator<Integer> it = nodes.get(i).iterator();
				    	while(it.hasNext()) {
				    		int current = it.next();
				    		Iterator<Integer> itnbrs = Graph.graph.get(current).neighbors.iterator();
				    		while(itnbrs.hasNext()) {
				    			int nbr = itnbrs.next();
				    			if (level[nbr] > level[current]) {
				    				float simnbr = 0;
				    				if (sims.get(current).containsKey(nbr))
				    					simnbr = sims.get(current).get(nbr);
				    				else 
				    					simnbr = updateSimsForFinalPrune(current, nbr, commonNeighbors(current, nbr), sims);
				    				records.get(current).add(nbr);
				    				reaches.get(current).add(nbr);
				    				dfsForFinalPrune(current, nbr, simnbr, directreach, records, reaches, sims);
				    			}
				    		}
				    	}
		    		} else {
		    			if (i == 0)
		    				break;
		    			Iterator<Integer> it = nodes.get(i).iterator();
				    	while(it.hasNext()) {
				    		int current = it.next();
				    		HashSet<Integer> sameLevel = new HashSet<Integer>(); 	
				    		HashSet<Integer> dishopnbr1 = new HashSet<Integer>();  
				    		HashSet<Integer> dishopnbr2 = new HashSet<Integer>();   
				    		dishopnbr1.add(current);
				    		for (int dis = 1; dis <= hop - i; dis++) {   
				    			if (sameLevel.size() == nodes.get(i).size())
				    				break;
				    			HashSet<Integer> currentHop = new HashSet<Integer>();
				    			for (Iterator<Integer> itdishopnbr = dishopnbr1.iterator(); itdishopnbr.hasNext(); ) {
				    				int dishopnbr = itdishopnbr.next();
				    				Iterator<Integer> hopnbrnbr = Graph.graph.get(dishopnbr).neighbors.iterator();
				    				while (hopnbrnbr.hasNext()) {
				    					int id = hopnbrnbr.next();
				    					dishopnbr2.add(id);
				    					if (!sameLevel.contains(id) && level[id] == level[current]) {
				    						sameLevel.add(id);	
				    						currentHop.add(id);
				    						reaches.get(current).add(id);
				    					}
				    					updateSimsForFinalPrune(current, id, sims.get(current).get(dishopnbr) * commonNeighbors(dishopnbr, id), sims);
				    				}
				    			}
				    			dishopnbr1.clear();
				    			dishopnbr1.addAll(dishopnbr2);
				    			dishopnbr2 = new HashSet<Integer>();
				    			for (Iterator<Integer> itcurrentHop = currentHop.iterator(); itcurrentHop.hasNext(); ) {
				    				int id = itcurrentHop.next();
				    				bfsForFinalPrune(current, id, sims.get(current).get(id), hop - i - dis, directreach, records, reaches, sims, level);
				    			}
				    		}
				    	}
		    		}
		    	}
		    }	
	}
	
	public void dfsForFinalPrune(int current, int nbr, float simnbr, HashMap<Integer, HashSet<Integer>> directreach, HashMap<Integer, ArrayList<Integer>> records, HashMap<Integer, HashSet<Integer>> reaches, HashMap<Integer, HashMap<Integer, Float>> sims) {

		for (Iterator<Integer> it = reaches.get(nbr).iterator(); it.hasNext(); ) {
			int id = it.next();
			if (sims.get(current).containsKey(id) && sims.get(current).get(id) > sims.get(current).get(nbr) * sims.get(nbr).get(id))
				continue;
			updateSimsForFinalPrune(current, id, sims.get(current).get(nbr) * sims.get(nbr).get(id), sims);
			reaches.get(current).add(id);
			if (records.containsKey(nbr) && records.get(nbr).contains(id)) {
				if (directreach.containsKey(current)) {
					directreach.get(current).add(id);
				} else {
			        HashSet<Integer> hs = new HashSet<Integer>();
			        hs.add(id);
			        directreach.put(current, hs);
				}
			}
		}
		
	}
	
	
	public void bfsForFinalPrune(int current, int nbr, float simnbr, int step, HashMap<Integer, HashSet<Integer>> directreach, HashMap<Integer, ArrayList<Integer>> records, HashMap<Integer, HashSet<Integer>> reaches, HashMap<Integer, HashMap<Integer, Float>> sims, int[] level) {
		Queue<Integer> q = new LinkedList<Integer>();
		HashMap<Integer, Integer> hm = new HashMap<Integer, Integer>();
		hm.put(nbr, 0);
		if (records.containsKey(nbr)) {
			for (int i = 0; i < records.get(nbr).size(); i++) {
				int idnbr = records.get(nbr).get(i);
				if (directreach.containsKey(current) && !directreach.get(current).contains(idnbr)) {
					if (sims.get(current).containsKey(idnbr)) {
						if (sims.get(current).get(idnbr) > sims.get(current).get(nbr) * sims.get(nbr).get(idnbr)) {
							continue;
						}
					}
					updateSimsForFinalPrune(current, idnbr, sims.get(current).get(nbr) * sims.get(nbr).get(idnbr), sims);					
					q.add(idnbr);
					hm.put(idnbr, 1);
					reaches.get(current).add(idnbr);
				}
			}
		}
		while (!q.isEmpty()) {
			int id = q.poll();
			if (hm.get(id) >= step)
				break;
			Iterator<Integer> it = Graph.graph.get(id).neighbors.iterator();
			while (it.hasNext()) {
				int idnbr = it.next();
				if (directreach.get(current).contains(idnbr))
					continue;
				if (hm.containsKey(idnbr)) {
					if (!sims.get(id).containsKey(idnbr))
						updateSimsForFinalPrune(id, idnbr, commonNeighbors(id, idnbr), sims);
					updateSimsForFinalPrune(current, idnbr, sims.get(current).get(id) * sims.get(id).get(idnbr), sims);
					continue;
				} else {
					if (!sims.get(id).containsKey(idnbr))
						updateSimsForFinalPrune(id, idnbr, commonNeighbors(id, idnbr), sims);
					updateSimsForFinalPrune(current, idnbr, sims.get(current).get(id) * sims.get(id).get(idnbr), sims);					
					q.add(idnbr);
					hm.put(idnbr, hm.get(id) + 1);
					reaches.get(current).add(idnbr);
				}
			}
		}
	}
  
    
    public void dealCmty(String cmtypath, String outpath, String split) throws IOException {
    	File file = new File(cmtypath);
		String encoding="GBK";
		HashMap<Integer, HashSet<Integer>> cmty = new HashMap<Integer, HashSet<Integer>>();
		
		if (file.isFile() && file.exists()) { 
            InputStreamReader read = new InputStreamReader(new FileInputStream(file),encoding);
            BufferedReader bufferedReader = new BufferedReader(read);
            String lineTxt = null;
            while ((lineTxt = bufferedReader.readLine()) != null) { 
            	String[] str = lineTxt.split(split);          	
            	int nodeid = Integer.valueOf(str[0]), clusterid = Integer.valueOf(str[1]);
            	if (Graph.id2newid.containsKey(nodeid)) {
		            if (cmty.containsKey(clusterid)) {
			            cmty.get(clusterid).add(Graph.id2newid.get(nodeid));
			        } else {
			            HashSet<Integer> hs = new HashSet<Integer>();
			            hs.add(Graph.id2newid.get(nodeid));
			            cmty.put(clusterid, hs);
			        }
            	}
            }
            read.close();
		}
		
		file = new File(outpath);
		BufferedWriter writer = null;
		writer = new BufferedWriter(new FileWriter(file));
		String EOL = System.getProperty("line.separator");
		for (Map.Entry<Integer, HashSet<Integer>> me : cmty.entrySet()) {
			Iterator<Integer> it = me.getValue().iterator();
			String str = "";
			while (it.hasNext()) {
				str += it.next();
				str += " ";
			}
			str.substring(str.length()-2, str.length()-1);
			writer.write(str + EOL);
		}
		writer.flush();
		writer.close();
    }
    
    
    public float commonNeighbors(int n1, int n2) {
    	Set<Integer> neighbors1 = Graph.graph.get(n1).neighbors;
    	Set<Integer> neighbors2 = Graph.graph.get(n2).neighbors;
    	int count = 0;
    	if (neighbors1.size() < neighbors2.size()) {
    		Iterator<Integer> it1 = neighbors1.iterator();
    		while(it1.hasNext()) {
    			int temp = it1.next();
    			if (neighbors2.contains(temp)) {
    				count++;
    			}
    		}
    	} else {
    		Iterator<Integer> it2 = neighbors2.iterator();
    		while(it2.hasNext()) {
    			int temp = it2.next();
    			if (neighbors1.contains(temp)) {
    				count++;
    			}
    		}
    	}
    	return (float) (count / Math.sqrt((neighbors1.size()) * (neighbors2.size())));
    }
    
    public float updateSimsForFinalPrune(int n1, int n2, float sim, HashMap<Integer, HashMap<Integer, Float>> sims) {
    	if (!sims.get(n1).containsKey(n2) || sim > sims.get(n1).get(n2)) {
    		sims.get(n1).put(n2, sim);
    		sims.get(n2).put(n1, sim);
    		return sim;
    	} else {
    		return sims.get(n1).get(n2);
    	}
    }

}
