/*
 * Copyright (c) 2010, The Broad Institute
 *
 * Permission is hereby granted, free of charge, to any person
 * obtaining a copy of this software and associated documentation
 * files (the "Software"), to deal in the Software without
 * restriction, including without limitation the rights to use,
 * copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following
 * conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
 * OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 * OTHER DEALINGS IN THE SOFTWARE.
 */

package org.broadinstitute.sting.gatk.phonehome;

import org.apache.log4j.Logger;
import org.broadinstitute.sting.commandline.CommandLineUtils;
import org.broadinstitute.sting.gatk.CommandLineGATK;
import org.broadinstitute.sting.gatk.GenomeAnalysisEngine;
import org.broadinstitute.sting.gatk.arguments.GATKArgumentCollection;
import org.broadinstitute.sting.gatk.walkers.Walker;
import org.broadinstitute.sting.utils.StingException;
import org.broadinstitute.sting.utils.Utils;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;
import org.simpleframework.xml.stream.Format;
import org.simpleframework.xml.stream.HyphenStyle;

import java.io.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.zip.GZIPOutputStream;


/**
 * @author depristo
 *         <p/>
 *         Class GATKRunReport
 *         <p/>
 *         A detailed description of a GATK run, and error if applicable
 */
public class GATKRunReport {
    /**
     * The root file system directory where we keep common report data
     */
    private static File REPORT_DIR = new File("/humgen/gsa-hpprojects/GATK/reports");

    /**
     * The full path to the direct where submitted (and uncharacterized) report files are written
     */
    private static File REPORT_SUBMIT_DIR = new File(REPORT_DIR.getAbsolutePath() + "/submitted");

    /**
     * Full path to the sentinel file that controls whether reports are written out.  If this file doesn't
     * exist, no long will be written
     */
    private static File REPORT_SENTINEL = new File(REPORT_DIR.getAbsolutePath() + "/ENABLE");

    /**
     * our log
     */
    protected static Logger logger = Logger.getLogger(GATKRunReport.class);


    // the listing of the fields is somewhat important; this is the order that the simple XML will output them
    @ElementList(required = true, name = "gatk_header_Information")
    private static List<String> mGATKHeader;

    @Element(required = false, name = "exception")
    private final ExceptionToXML mException;

    @Element(required = true, name = "argument_collection")
    private final GATKArgumentCollection mCollection;

    @Element(required = true, name = "working_directory")
    private static String currentPath;

    @Element(required = true, name = "start_time")
    private static String startTime;

    @Element(required = true, name = "end_time")
    private static String endTime;

    @Element(required = true, name = "run_time")
    private static long runTime;

    @Element(required = true, name = "command_line")
    private static String cmdLine;

    @Element(required = true, name = "walker_name")
    private static String walkerName;

    @Element(required = true, name = "svn_version")
    private static String svnVersion;

    @Element(required = true, name = "memory")
    private static long memory;

    @Element(required = true, name = "java_tmp_directory")
    private static String tmpDir;

    @Element(required = true, name = "domain_name")
    private static String domainName;

    @Element(required = true, name = "user_name")
    private static String userName;

    @Element(required = true, name = "host_name")
    private static String hostName;

    @Element(required = true, name = "java")
    private static String java;

    @Element(required = true, name = "machine")
    private static String machine;

    @Element(required = true, name = "iterations")
    private static long nIterations;

    @Element(required = true, name = "reads")
    private static long nReads;

    @Element(required = true, name = "read_metrics")
    private static String readMetrics;

    // not done
    //- walker-specific args
    //+ md5 all filenames
    //- size of filenames
    //- # reads/loci
    //- free memory on machine

    public enum PhoneHomeOption {
        NO_ET,
        STANDARD,
        STDOUT
    }

    private static final DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH.mm.ss");

    static {
        GATKRunReport.mGATKHeader = CommandLineGATK.createApplicationHeader();
        currentPath = System.getProperty("user.dir");
    }

    /** Create a new RunReport and population all of the fields with values from the walker and engine */
    public GATKRunReport(Walker<?,?> walker, Exception e, GenomeAnalysisEngine engine) {
        this.mCollection = engine.getArguments();
        this.mException = e == null ? null : new ExceptionToXML(e);

        startTime = dateFormat.format(engine.getStartTime()); // fixme
        Date end = new java.util.Date();
        endTime = dateFormat.format(end);
        runTime = (end.getTime() - engine.getStartTime().getTime()) / 1000L; // difference in seconds

        cmdLine = CommandLineUtils.createApproximateCommandLineArgumentString(engine, walker);
        walkerName = engine.getWalkerName(walker.getClass());
        svnVersion = CommandLineGATK.getVersionNumber();
        nIterations = engine.getCumulativeMetrics().getNumIterations();
        nReads = engine.getCumulativeMetrics().getNumReadsSeen();
        readMetrics = engine.getCumulativeMetrics().toString();
        memory = Runtime.getRuntime().totalMemory();
        tmpDir = System.getProperty("java.io.tmpdir");
        domainName = "Need to figure this out";
        hostName = "Need to figure this out";
        userName = System.getProperty("user.name");
        java = Utils.join("-", Arrays.asList(System.getProperty("java.vendor"), System.getProperty("java.version")));
        machine = Utils.join("-", Arrays.asList(System.getProperty("os.name"), System.getProperty("os.arch")));
    }

    public void postReport(OutputStream stream) {
        Serializer serializer = new Persister(new Format(new HyphenStyle()));
        try {
            serializer.write(this, stream);
            //throw new StingException("test");
        } catch (Exception e) {
            throw new StingException("Failed to marshal the data to the file " + stream, e);
        }
    }

    public void postReport(File destination) throws FileNotFoundException, IOException {
        BufferedOutputStream out =
                new BufferedOutputStream(
                        new GZIPOutputStream(
                                new FileOutputStream(destination)));
        try {
            postReport(out);
        } finally {
            out.close();
        }
    }

    public void postReport() {
        try {
            if ( sentinelExists() ) {
                String filename = org.apache.commons.lang.RandomStringUtils.randomAlphanumeric(32) + ".report.xml.gz";
                File file = new File(REPORT_SUBMIT_DIR, filename);
                postReport(file);
                logger.info("Wrote report to " + file);
            } else {
                logger.info("Not writing report: sentinel " + REPORT_SENTINEL + " doesn't exist");
            }
        } catch ( Exception e ) {
            // we catch everything, and no matter what eat the error
            logger.warn("Received error while posting report.  GATK continuing on but no run report has been generated because: " + e.getMessage());
        }
    }

    private boolean sentinelExists() {
        return REPORT_SENTINEL.exists();
    }

    class ExceptionToXML {
        @Element(required = false, name = "message")
        String message = null;

        @ElementList(required = false, name = "stacktrace")
        final List<String> stackTrace = new ArrayList<String>();

        @Element(required = false, name = "cause")
        ExceptionToXML cause = null;
        
        public ExceptionToXML(Throwable e) {
            message = e.getMessage();
            for (StackTraceElement element : e.getStackTrace()) {
                stackTrace.add(element.toString());
            }

            if ( e.getCause() != null ) {
                //message += " because " + e.getCause().getMessage();
                cause = new ExceptionToXML(e.getCause());
            }
        }
    }
}
