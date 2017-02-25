package WordCount.WordCount;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.StringTokenizer;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.ArrayWritable;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.MapWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.Writable;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.Mapper.Context;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

import com.google.common.collect.Iterables;

public class posfind {
	public static class TokenizerMapper extends Mapper<Object, Text, Text, IntWritable >{
		private Text word=new Text();
		private IntWritable one=new IntWritable(1);
		 HashMap<Integer, Integer> Wordcount=new HashMap<Integer, Integer>();
		 HashMap<Integer, Integer> Palindromes=new HashMap<Integer, Integer>();
		 HashMap<Integer, HashMap<String, Integer>> PartsOfSpeech=new HashMap<Integer, HashMap<String, Integer>>();
		  char c;
		  public boolean isPalindrome(String word){
				StringBuffer word1=new StringBuffer(word);
				StringBuffer wordreverse = new StringBuffer(word1.reverse());
			      if (word1.equals(wordreverse)){
			    	  return true;
			      }
			      else
			    	  return false;
			}
		  
		  public void map(Object key, Text value, Context context
                  ) throws IOException, InterruptedException {
			  String i=null;
			  String x=null;
			  int index;
			  Path path=new Path("pos.txt");
			  FileSystem fs=FileSystem.get(new Configuration());  
    StringTokenizer itr = new StringTokenizer(value.toString());
    while (itr.hasMoreTokens()) {
    	String s=itr.nextToken();
    	if(s.length()>=5){
    		if(isPalindrome(s)){
				if (Palindromes.containsKey(s.length()))
				Palindromes.put(s.length(),Palindromes.get(s.length())+1);
				else
					Palindromes.put(s.length(),1);
				}
    		  BufferedReader br=new BufferedReader(new InputStreamReader(fs.open(path)));
    	while((i=br.readLine())!=null){
    			index= i.indexOf('$');
    			if(i.contains(s)){
    			if(index!=-1 && index<i.length())
    			x=i.substring(0,index);
    			if(s.equals(x)){
    				c=i.charAt(i.length()-1);
    				String pos=getpos(c);
    				if(pos!=""){
    					if(Wordcount.containsKey(s.length()))
    	    				Wordcount.put(s.length(),Wordcount.get(s.length())+1);
    	    				else
    	    					Wordcount.put(s.length(),1);
    	    				
    					HashMap<String, Integer> hm4=new HashMap<String, Integer>();
    				if(PartsOfSpeech.containsKey(s.length())){
    				     hm4.putAll(PartsOfSpeech.get(s.length()));
    				     if(hm4.containsKey(pos)){
    				    	 hm4.put(pos,hm4.get(pos)+1);
    				     }
    				     PartsOfSpeech.put(s.length(),hm4);
    				}
    				else{
    					hm4.put(pos,1);
    					PartsOfSpeech.put(s.length(),hm4);
    				}
    				break;
    				}
    			}	
    			}
    	}  
    	}
    }
    
    for(Entry<Integer, Integer> e1 : Wordcount.entrySet()){
    	Integer keyWordcount=e1.getKey();
    word.set(keyWordcount+ " "+ "count of words");
    one.set(Wordcount.get(keyWordcount));
    context.write(word,one);
    }
    
    for(Entry<Integer, Integer> e2 : Palindromes.entrySet()){
    	Integer keyPalindromes=e2.getKey();
    word.set(keyPalindromes + " "+ "No. of Palindromes");
    one.set(Palindromes.get(keyPalindromes));
    context.write(word,one);
    }
    
    for(Entry<Integer,HashMap<String,Integer>> e3 : PartsOfSpeech.entrySet()){
    HashMap<String, Integer> temp=new HashMap<String, Integer>();
    Integer keyPartsOfSpeech=e3.getKey();
    temp=PartsOfSpeech.get(keyPartsOfSpeech);
    for(Entry<String,Integer> e : temp.entrySet() ){
   	 String s1=keyPartsOfSpeech + " " + e.getKey();
   	 word.set(s1);
   	 one.set(e.getValue());
   	 context.write(word,one);
    }
    }   
 }
		  public String getpos(char c){
			 String pos="";
			  if(c=='N'){
					 pos="Noun";
					 return pos;
				}
				else if(c=='p'){
				     pos="Plural";
					 return pos;
				}
				else if(c=='t'|| c=='V' || c=='i'){
				 pos="Verb";
					 return pos;
				}
				else if(c=='A'){
					pos="Adjective";
					 return pos;
				}
				else if(c=='r'){
					pos="Pronoun";
					 return pos;
				}
				else if(c=='C'){
					pos="Conjunction";
					 return pos;
				}
				else if(c=='P'){
					 pos="Preposition";
					 return pos;
				}
				else if(c=='!'){
					 pos="Interjection";
					 return pos;
				}
			return pos;
		  }
  }
	public static class IntSumReducer
    extends Reducer<Text,IntWritable,Text,IntWritable> {
	private IntWritable value=new IntWritable();
 public void reduce(Text key, Iterable<IntWritable> values,
                    Context context
                    ) throws IOException, InterruptedException {
	 int sum = 0;
		for (IntWritable val : values) {

			sum += val.get();
		}
		value.set(sum);
		context.write(key, value);
context.write(key,value);
 }
}
	public static void main(String[] args) throws Exception {
	    Configuration conf = new Configuration();
	    conf.set("mapred.job.tracker", "hdfs://cshadoop1:61120");
	    conf.set("yarn.resourcemanager.address", "cshadoop1.utdallas.edu:8032");
	    conf.set("mapreduce.framework.name", "yarn");
	    Job job = Job.getInstance(conf, "parts of speech");	
	    job.setJarByClass(posfind.class);
	    job.setMapperClass(TokenizerMapper.class);
	    job.setReducerClass(IntSumReducer.class);
	    job.setMapOutputKeyClass(Text.class);
	    job.setMapOutputValueClass(IntWritable.class);
	    job.setOutputKeyClass(Text.class);
	    job.setOutputValueClass(IntWritable.class);
	    FileInputFormat.addInputPath(job, new Path(args[0]));
	    FileOutputFormat.setOutputPath(job, new Path(args[1]));
	    System.exit(job.waitForCompletion(true) ? 0 : 1);
	  }
}
