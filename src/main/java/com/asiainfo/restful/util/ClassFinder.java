package com.asiainfo.restful.util;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * 
 * @Description: Load classes by name or by package name.
 * 
 * @author       zq
 * @date         2017年10月13日  下午1:07:15
 * Copyright: 	  北京亚信智慧数据科技有限公司
 */
public class ClassFinder {

    final Log log = LogFactory.getLog(getClass());
    final Pattern classFileNamePattern = Pattern.compile("^[A-Za-z0-9\\_]+\\.class$");

    /**
     * 
     * @Description: Load classes by name or by package name.
     * 
     * @param args
     * @return
     */
    public List<Class<?>> findClasses(String... args) {
    	
        List<Class<?>> list = new ArrayList<Class<?>>();
        for (String name : args) {
            Class<?> clazz = findClassByName(name);
            if (clazz != null) {
                list.add(clazz);
            }
            else {
                list.addAll(findClassesByPackage(name));
            }
        }
        return list;
    }

    /**
     * 
     * @Description: 通过类名查找类
     * 
     * @param name
     * @return
     */
    protected Class<?> findClassByName(String name) {
    	
        try {
            Class<?> clazz = Class.forName(name);
            log.info("Found class: " + name);
            return clazz;
        }
        catch (ClassNotFoundException e) {
            return null;
        }
    }

    /**
     * 
     * @Description: 通过包名查找类
     * 
     * @param pkgName
     * @return
     */
    protected List<Class<?>> findClassesByPackage(String pkgName) {
    	
        URL url = getClass().getClassLoader().getResource(pkgName.replace('.', '/'));
        if (url == null) {
            throw new RuntimeException("Package not found: " + pkgName);
        }
        String theUrl = url.toString();
        if (theUrl.startsWith("file:")) {
            return findClassesInDir(pkgName, url.getPath());
        }
        if (theUrl.startsWith("jar:file:") && theUrl.endsWith("!/" + pkgName.replace('.', '/'))) {
            return findClassesInJar(pkgName, theUrl.substring(9, theUrl.length() - pkgName.length() - 2));
        }
        throw new RuntimeException("Package not found: " + pkgName);
    }

    /**
     * 
     * @Description: 在classes目录查找
     * 
     * @param pkgName
     * @param path
     * @return
     */
    protected List<Class<?>> findClassesInDir(String pkgName, String path) {
    	
        File dir = new File(path);
        String[] files = dir.list();
        return Arrays.asList(files).stream().filter((fileName) -> {
            return fileName.endsWith(".class") && classFileNamePattern.matcher(fileName).matches();
        }).map((fileName) -> {
            // try load class
            try {
                String className = pkgName + "." + fileName.substring(0, fileName.length() - 6);
                Class<?> clazz = Class.forName(className);
                log.info("Found class " + className + " in file: " + fileName);
                return clazz;
            }
            catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        }).filter((clazz) -> {
            return clazz != null;
        }).collect(Collectors.toList());
    }

    /**
     * 
     * @Description: 在jar中查找
     * 
     * @param pkgName
     * @param jarFile
     * @return
     */
    protected List<Class<?>> findClassesInJar(String pkgName, String jarFile) {
    	
        log.info("Scan classes in jar: " + jarFile);
        List<Class<?>> list = new ArrayList<Class<?>>();
        String packagePrefix = pkgName + ".";
        ZipInputStream zip = null;
        try {
            zip = new ZipInputStream(new BufferedInputStream(new FileInputStream(jarFile)));
            for (ZipEntry entry = zip.getNextEntry(); entry != null; entry = zip.getNextEntry()) {
                String entryName = entry.getName();
                if (!entry.isDirectory() && entryName.endsWith(".class")) {
                    String classFileName = entryName.replace('/', '.');
                    if (classFileName.startsWith(packagePrefix) && classFileNamePattern.matcher(classFileName.substring(packagePrefix.length())).matches()) {
                        log.info("Found class " + classFileName + " in jar: " + jarFile);
                        try {
                            list.add(Class.forName(classFileName.substring(0, classFileName.length() - 6)));
                        }
                        catch (ClassNotFoundException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
            }
            return list;
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
        finally {
            if (zip != null) {
                try {
                    zip.close();
                }
                catch (IOException e) {
                }
            }
        }
    }

}
