package lpbc;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Random;
import java.util.Stack;

import preprogressing.Graph;

public class RandomWalk {
	
	public static HashSet<Integer> core = new HashSet<Integer>(); 
	public static int diffedgecen = 0, minedgecen = 0, maxedgecen = Integer.MIN_VALUE;
	
	public void startWalk(int k, int itertimes, double alpha, double epsilon1, double epsilon2) {
		
		Stack<Integer> s = new Stack<Integer>();
		HashMap<Integer, HashMap<Integer, Integer>> edgecens = new HashMap<Integer, HashMap<Integer, Integer>>();
		for (int i = 0; i < Graph.nodenum; i++) {
			Iterator<Integer> it = Graph.graph.get(i).neighbors.iterator();
			HashMap<Integer, Integer> hm = new HashMap<Integer, Integer>();
			while (it.hasNext()) {
				hm.put(it.next(), 1);
			}
			edgecens.put(i, hm);
		}
		for (int i = 0; i < itertimes; i++) {	
			int edgecount = 0;
			Random ra = new Random();
			boolean[] vstnode = new boolean[Graph.nodenum];
			int root = ra.nextInt(Graph.nodenum), current = 0; 
			s.push(root);	
			vstnode[root] = true;
			int index = ra.nextInt(Graph.graph.get(root).neighbors.size()), count1 = 1;
			Iterator<Integer> itroot = Graph.graph.get(root).neighbors.iterator();
			while (itroot.hasNext() && count1++ < index) {
				current = itroot.next();
			}
			s.push(current);
			HashMap<Integer, HashSet<Integer>> visited = new HashMap<Integer, HashSet<Integer>>();
			HashSet<Integer> temp = new HashSet<Integer>();
			temp.add(current);
			visited.put(root, temp);
			temp.clear();
			temp.add(root);
			visited.put(current, temp);
			vstnode[current] = true;
			edgecount++;
			while (edgecount < k) {				
				if (ra.nextDouble() > (1 - alpha)) {  
					boolean returnpa = true;
					for (int j = 0; j < Graph.graph.get(current).neighbors.size(); j++) {
						int increase = ra.nextInt(Graph.graph.get(current).neighbors.size()), count = 1, key = 0;
						Iterator<Integer> it = Graph.graph.get(current).neighbors.iterator();
						key = it.next();
						while (it.hasNext() && ++count < increase) {
							key = it.next();
						}
						if (vstnode[key] == false) {
							int value = edgecens.get(current).get(key); 
							if (value == 1 || !visited.containsKey(current) || !visited.get(current).contains(key)) { 
								s.push(key);
								edgecens.get(current).put(key, value + 1);
								edgecens.get(key).put(current, value + 1);
								returnpa = false;
								edgecount++;
								if (visited.containsKey(current)) {   
									visited.get(current).add(key);
								} else {
									HashSet<Integer> hs = new HashSet<Integer>();
									hs.add(key);
									visited.put(current, hs);
								}
								if (visited.containsKey(key)) {
									visited.get(key).add(current);
								} else {
									HashSet<Integer> hs = new HashSet<Integer>();
									hs.add(current);
									visited.put(key, hs);
								}
								if (value + 1 > maxedgecen) 
									maxedgecen = value + 1;
								current = key;
								break;
							}
				        }
					}
					if (returnpa) {
						Iterator<Integer> it = Graph.graph.get(current).neighbors.iterator();
						boolean flag = false;
						while (it.hasNext()) {
							int id = it.next();
							if (vstnode[id] == false) {
								flag = true;
								s.push(id);
								edgecens.get(current).put(id, edgecens.get(current).get(id) + 1);
								edgecens.get(id).put(current, edgecens.get(id).get(current) + 1);
								edgecount++;
								current = id;
								break;
							}
						}
						if (!flag)
							s.pop();
					}
				} else { 
					s.pop();
				}
				if (s.peek() == root) { 
					for(Iterator<Integer> it = Graph.graph.get(root).neighbors.iterator(); it.hasNext();) {							
						current = it.next();
						if (vstnode[current] == false) {
							s.push(current);	
							break;
						}
					}   
					edgecount++;
				}
			}
			s.clear();
		}
		diffedgecen = maxedgecen - minedgecen;
		
		float maxcen = Integer.MIN_VALUE, mincen = Integer.MAX_VALUE;
		for (int i = 0; i < Graph.nodenum; i++) {
			float centrality = 0;
			for(Iterator<Integer> it = Graph.graph.get(i).neighbors.iterator(); it.hasNext();) { 
				int neighbor = it.next();
				centrality += (float) Math.log10(edgecens.get(i).get(neighbor) + 1) / (float) Math.log10(maxedgecen);   
				centrality += (float) edgecens.get(i).get(neighbor) / itertimes;
			}
			Graph.graph.get(i).centrality = centrality;
			if (centrality > maxcen)
				maxcen = centrality;
			if (centrality < mincen)
				mincen = centrality;
		}
		
		for (int i = 0; i < Graph.nodenum; i++) {  
			Graph.graph.get(i).centrality = (float) ((float) Math.log10(Graph.graph.get(i).centrality) / (float) Math.log10(maxcen));
			if (Graph.graph.get(i).centrality >= epsilon1 && Graph.graph.get(i).centrality <= epsilon2) {
                int count = 0;
				for (Iterator<Integer> it = core.iterator(); it.hasNext();) {
					int id = it.next();
					if (!Graph.graph.get(i).neighbors.contains(id))
						count++;
				}
				if (count == core.size()) 
					core.add(i);
				
			}
		}
		System.out.println("size of core node set:" + core.size());
	}
	
    
}
