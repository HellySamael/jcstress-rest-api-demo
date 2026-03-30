package com.example.demo.domain;

import org.openjdk.jcstress.annotations.Actor;
import org.openjdk.jcstress.annotations.Description;
import org.openjdk.jcstress.annotations.Expect;
import org.openjdk.jcstress.annotations.JCStressTest;
import org.openjdk.jcstress.annotations.Outcome;
import org.openjdk.jcstress.annotations.State;

import com.example.demo.counter.jmm.AppCounter;
import org.openjdk.jcstress.infra.results.II_Result;


public class AppCounterStressTest {

    private final AppCounter counter = new AppCounter();

}
