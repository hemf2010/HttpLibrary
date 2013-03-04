package com.android.http.net;

import java.util.ArrayList;

import android.text.TextUtils;


/**
 * 在发起网络请求时，用来存放请求参数的容器
 */
public class HttpParameters {

	private ArrayList<String> mKeys = new ArrayList<String>();
	private ArrayList<String> mValues=new ArrayList<String>();
	
	private ArrayList<String> mFileKeys = new ArrayList<String>();
	private ArrayList<FileWrapper> mFiles = new ArrayList<FileWrapper>();
	
	public HttpParameters(){
		
	}
	
	public void add(String key, String value){
	    if(!TextUtils.isEmpty(key)&&!TextUtils.isEmpty(value)){
	        this.mKeys.add(key);
	        mValues.add(value);
	    }
	   
	}
	
	public void add(String key, int value){
	    this.mKeys.add(key);
        this.mValues.add(String.valueOf(value));
	}
	public void add(String key, long value){
	    this.mKeys.add(key);
        this.mValues.add(String.valueOf(value));
    }
	public void add(String key, FileWrapper file){
		this.mFileKeys.add(key);
		this.mFiles.add(file);
	}
	public void remove(String key){
	    int firstIndex=mKeys.indexOf(key);
	    if(firstIndex>=0){
	    	if(mKeys.contains(key)){
	    		this.mKeys.remove(firstIndex);
	    		this.mValues.remove(firstIndex);
	    	}else if(mFileKeys.contains(key)){
	    		this.mFileKeys.remove(firstIndex);
	    		this.mFiles.remove(firstIndex);
	    	}
	    }
	  
	}
	
	public void remove(int i){
	    if(i<mKeys.size()){
	        mKeys.remove(i);
	        this.mValues.remove(i);
	    }
		
	}
	public void removeFileIndex(int i){
		if(i<mFileKeys.size()&&i>=0){
			mFileKeys.remove(i);
			this.mFiles.remove(i);
		}
	}
	
	private int getLocation(String key){
		if(this.mKeys.contains(key)){
			return this.mKeys.indexOf(key);
		}else if(this.mFileKeys.contains(key)){
			return this.mFileKeys.indexOf(key);
		}
		return -1;
	}
	
	public String getKey(int location){
		if(location >= 0 && location < this.mKeys.size()){
			return this.mKeys.get(location);
		}
		return "";
	}
	public String getFileKey(int location){
		if(location >= 0 && location < this.mFileKeys.size()){
			return this.mFileKeys.get(location);
		}
		return "";
	}
	
	public String getValue(String key){
	    int index=getLocation(key);
	    if(index>=0 && index < this.mKeys.size()){
	        return  this.mValues.get(index);
	    }
	    else{
	        return null;
	    }
		
		
	}
	public FileWrapper getFile(String key){
	    int index=getLocation(key);
	    if(index>=0 && index < this.mFileKeys.size()){
	        return  this.mFiles.get(index);
	    }
	    else{
	        return null;
	    }
		
		
	}
	public String getValue(int location){
	    if(location>=0 && location < this.mKeys.size()){
	        String rlt = this.mValues.get(location);
	        return rlt;
	    }
	    else{
	        return null;
	    }
		
	}
	public FileWrapper getFile(int location){
	    if(location>=0 && location < this.mFileKeys.size()){
	    	FileWrapper file = this.mFiles.get(location);
	        return file;
	    }
	    else{
	        return null;
	    }
		
	}
	
	public int size(){
		return mKeys.size();
	}
	
	public int fileSize(){
		return mFileKeys.size();
	}
	
	public void addAll(HttpParameters parameters){
		for(int i = 0; i < parameters.size(); i++){
			this.add(parameters.getKey(i), parameters.getValue(i));
		}
		
	}
	
	public void clear(){
		this.mKeys.clear();
		this.mValues.clear();
		this.mFileKeys.clear();
		this.mFiles.clear();
	}
}
