package WordCount.WordCount;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;
import java.util.Set;
import java.util.StringTokenizer;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapred.TextInputFormat;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
//import org.apache.hadoop.mapreduce.Mapper.Context;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

//import WordCount.WordCount.WordCount.IntSumReducer;
//import WordCount.WordCount.WordCount.TokenizerMapper;

public class Posnegtest{
	
	 public static class TokenizerMapper
     extends Mapper<Object, Text, Text, IntWritable>{
  private final static IntWritable one = new IntWritable(1);
  private Text word = new Text("Positive words");
  private Text word1=new Text("Negative words");
 // System.out.println(word);
  public void map(Object key, Text value, Context context
                  ) throws IOException, InterruptedException {
	ArrayList<String> al=new ArrayList<String>(200);
	ArrayList<String> al1=new ArrayList<String>(200);
	HashMap<String, Integer> hm=new HashMap<String, Integer>();
	
	  Path p=new Path("positive-words.txt");
	  Path p1=new Path("negative-words.txt");
	  FileSystem fs=FileSystem.get(new Configuration());
	  String line;
		BufferedReader br=new BufferedReader(new InputStreamReader(fs.open(p)));
    	while((line=br.readLine())!=null){
    		String y=line;
    		if(y.contains(";")){
    		continue;
    		}
    		else{
    			al.add(y);
    		}
    	}
    	BufferedReader br1=new BufferedReader(new InputStreamReader(fs.open(p1)));
    	while((line=br1.readLine())!=null){
    		String y=line;
    		if(y.contains(";")){
    		continue;
    		}
    		else{
    			al1.add(y);
    		}
    	}
    StringTokenizer itr = new StringTokenizer(value.toString());
    while (itr.hasMoreTokens()) {
    	String x=itr.nextToken(); 
           for(String i:al){
    			if(x.equals(i)){
    		         Integer count = hm.get("positive words");
    		         if(count == null) count = new Integer(0);
    		         count+=1;
    		         hm.put("positive words",count);
  		  }
           }
           for(String j:al1){
   			if(x.equals(j)){
   				//String token = itr.nextToken();
		         Integer count = hm.get("negative words");
		         if(count == null) count = new Integer(0);
		         count+=1;
		         hm.put("negative words",count);
 		  }
          }
    		}
	Set<String> keys = hm.keySet();
	for (String s : keys) {
        word.set(s);
        one.set(hm.get(s));
        context.write(word,one);
	}
  	  }
    }

public static class IntSumReducer
     extends Reducer<Text,IntWritable,Text,IntWritable> {
  private IntWritable result = new IntWritable();

  public void reduce(Text key, Iterable<IntWritable> values,
                     Context context
                     ) throws IOException, InterruptedException {
    int sum = 0;
    for (IntWritable val : values) {
      sum += val.get();
    }
    result.set(sum);
    context.write(key, result);
  }
}


	public static void main(String[] args) throws Exception {
	    Configuration conf = new Configuration();
	    conf.set("mapred.job.tracker", "hdfs://cshadoop1:61120");
	    conf.set("yarn.resourcemanager.address", "cshadoop1.utdallas.edu:8032");
	    conf.set("mapreduce.framework.name", "yarn");
	    Job job = Job.getInstance(conf, "positive negative");	
	    job.setJarByClass(Posnegtest.class);
	   // job.setInputFormatClass(TextInputFormat.class);
	    job.setMapperClass(TokenizerMapper.class);
	    //job.setCombinerClass(IntSumReducer.class);
	    job.setReducerClass(IntSumReducer.class);
	    job.setOutputKeyClass(Text.class);
	    job.setOutputValueClass(IntWritable.class);
	    FileInputFormat.addInputPath(job, new Path(args[0]));
	    FileOutputFormat.setOutputPath(job, new Path(args[1]));
	    System.exit(job.waitForCompletion(true) ? 0 : 1);
	  }
}
