package lpbc;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Set;

import calauc.Auc;
import preprogressing.Graph;

public class CalSims implements Runnable {

	int hop = 0;
	Similarity s;
	int id;
	Set<Integer> syncore;
	String temppath;
	
	CalSims(int hop, int id, Set<Integer> syncore, String temppath) {
		this.hop = hop;
		this.id = id;
		this.syncore = syncore;
		this.temppath = temppath;
	}
	@Override
	public void run() {
		// TODO Auto-generated method stub
        this.s = new Similarity();
        try {
			this.s.calSimilarityForThread(hop, this.temppath,  this.id, this.syncore);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void main(String args[]) throws IOException {
		
		int k = Integer.parseInt(args[0]), itertime = Integer.parseInt(args[1]), hop = Integer.parseInt(args[6]);
		double alpha = Double.parseDouble(args[2]), epsilon1 = Double.parseDouble(args[3]), epsilon2 = Double.parseDouble(args[4]);
		double delta = Double.parseDouble(args[5]);
		String dsname = args[7];
		
		Graph g= new Graph();
		try {
			g.read(" ", "./dataset/" + dsname + '/' + dsname + ".txt");
		} catch (NumberFormatException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		RandomWalk rw = new RandomWalk();
		
		System.out.println("start the random walk");
		rw.startWalk(k, itertime, alpha, epsilon1, epsilon2);
		System.out.println("random walk over");	
		
		Auc auc = new Auc();
		Similarity s = new Similarity();
		String cmtypath = "./dataset/" + dsname + "/cmty.txt", newcmtypath = "./community/" + dsname + ".txt";
		auc.cmtypath = newcmtypath;
		
		File path = new File("./community");
		if (!path.exists()) {
			path.mkdirs();
		}
		path = new File("./temp_sims");
		if (!path.exists()) {
			path.mkdirs();
		}
		path = new File("./temp_sims/" + dsname);
		if (!path.exists()) {
			path.mkdirs();
		}
		path = new File("./results");
		if (!path.exists()) {
			path.mkdirs();
		}
		
		try {
			s.dealCmty(cmtypath, newcmtypath, " ");
			auc.getEbc(" ");
			auc.removeEdge();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
		
		Set<Integer> syncore = Collections.synchronizedSet(RandomWalk.core);
		
		CalSims cs1 = new CalSims(hop,  1, syncore, "./temp_sims/" + dsname + "/");
		CalSims cs2 = new CalSims(hop, 2, syncore, "./temp_sims/" + dsname + "/");
		CalSims cs3 = new CalSims(hop, 3, syncore, "./temp_sims/" + dsname + "/");
		CalSims cs4 = new CalSims(hop, 4, syncore, "./temp_sims/" + dsname + "/");
		Thread t1 = new Thread(cs1);
		t1.start();
		Thread t2 = new Thread(cs2);
		t2.start();
		Thread t3 = new Thread(cs3);
		t3.start();
		Thread t4 = new Thread(cs4);
		t4.start();
        try {
			t1.join();
			t2.join();
			t3.join();
			t4.join();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

        try {
			auc.writeSamplesForFinalPrune("./temp_sims/" + dsname + "/", "./results/" + dsname + ".txt", delta, dsname);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

}
