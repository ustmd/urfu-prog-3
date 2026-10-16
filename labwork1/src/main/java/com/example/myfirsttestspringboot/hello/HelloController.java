package com.example.myfirsttestspringboot.hello;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

@RestController
public class HelloController {

    private ArrayList<String> arrayList;
    private HashMap<Integer, String> hashMap;

    @GetMapping("/hello")
    public String hello(@RequestParam(value = "name", defaultValue = "World") String name) {
        return "Hello, " + name + "!";
    }

    @GetMapping("/update-array")
    public String updateArrayList(@RequestParam(value = "s", defaultValue = "") String s) {
        if (arrayList == null) {
            arrayList = new ArrayList<>();
        }
        if (!s.isEmpty()) {
            arrayList.add(s);
        }
        return "ArrayList: " + arrayList;
    }

    @GetMapping("/show-array")
    public ArrayList<String> showArrayList() {
        if (arrayList == null) {
            arrayList = new ArrayList<>();
        }
        return arrayList;
    }

    @GetMapping("/update-map")
    public String updateHashMap(@RequestParam(value = "s", defaultValue = "") String s) {
        if (hashMap == null) {
            hashMap = new HashMap<>();
        }
        if (!s.isEmpty()) {
            hashMap.put(hashMap.size() + 1, s);
        }
        return "HashMap: " + hashMap;
    }

    @GetMapping("/show-map")
    public Map<Integer, String> showHashMap() {
        if (hashMap == null) {
            hashMap = new HashMap<>();
        }
        return hashMap;
    }

    @GetMapping("/show-all-lenght")
    public String showAllLenght() {
        int arraySize = arrayList == null ? 0 : arrayList.size();
        int mapSize = hashMap == null ? 0 : hashMap.size();
        return "Количество элементов в ArrayList: " + arraySize
                + "; количество элементов в HashMap: " + mapSize;
    }
}
