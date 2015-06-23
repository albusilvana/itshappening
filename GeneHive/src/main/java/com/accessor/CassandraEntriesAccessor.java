package com.accessor;

import java.sql.SQLException;
import java.util.*;

import com.DTO.BasicEntityDTO;
import com.DTO.ExportEntityDTO;
import com.DTO.SearchOptionsDTO;
import com.Service.DateUtils;
import com.Model.Entity;
import com.Service.UtilsService;
import com.datastax.driver.core.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.Model.Entry;

import com.google.inject.Singleton;


/**
 * Created by silvana.albert on 3/29/15.
 */
@Singleton
public class CassandraEntriesAccessor {


    private static final Logger LOG = LoggerFactory.getLogger(CassandraEntriesAccessor.class);

    public static final String FIND_ALL_ENTRIES = "select identificationNumber,countrycode,mutation from Entries_Space.Entries";

    public static final String FIND_ALL_MUTATIONS = "select mutation from Entries_Space.Entries";

    public static final String FIND_ALL_MUTATIONS_FOR_CSV = "select name,identificationNumber,countryCode,mutation,professionalExposure,gender,dateOfBirth,dateOfDiagnosis,dateOfDeath,physician from Entries_Space.Entries";

    private UtilsService utilsService = new UtilsService();

    private Cluster cluster;

    private Session session;

    private volatile boolean active;
    private volatile boolean activating;
    private volatile boolean deactivating;

    private int insertBatchSize;


    public void activate() throws Exception {
        LOG.trace("Activating CasandraEventsAccessor");
        if (active || activating) {
            return;
        }
        activating = true;

        insertBatchSize = 1000;

        Cluster cluster = new Cluster.Builder()
                .addContactPoints("127.0.0.1")
                .withPort(9042)
                .build();

        String keyspace = "entries_space";
        session = cluster.connect(keyspace);

        active = true;
        activating = false;
        LOG.debug("CasandraEventsAccessor is activated");

    }


    public List<ExportEntityDTO> getCSVEntries() throws SQLException {
        if (!active) {
            LOG.error("CasandraEventsAccessor is not active");
        }
        List<ExportEntityDTO> results = new ArrayList<ExportEntityDTO>();
        ResultSet resultSet = session.execute(FIND_ALL_MUTATIONS_FOR_CSV);
        Iterator<Row> iter = resultSet.iterator();
//        while (iter.hasNext()) {
//            if (resultSet.getAvailableWithoutFetching() == 100 && !resultSet.isFullyFetched())
//                resultSet.fetchMoreResults();
//            Row row = iter.next();
//            String name = row.getString(0);
//            String identificationNumber = row.getString(1);
//            String countryCode = row.getString(2);
//            String mutationentries = row.getString(3);
//            String professionalExposures = row.getString(4);
//            String gender = row.getString(5);
//            Date dateOfBirth = row.getDate(6);
//            Date dateOfDiagnosis = row.getDate(7);
//            Date dateOfDeath = row.getDate(8);
//            String physician = row.getString(9);
//            int dateOfDeathAge = 100;
//            if (dateOfDeath != null) {
//                dateOfDeathAge = DateUtils.getAge(dateOfDeath);
//            }
//            ExportEntityDTO trainingModelDTO = new ExportEntityDTO(name, identificationNumber, countryCode, mutationentries, professionalExposures, gender, DateUtils.getAge(dateOfBirth), DateUtils.getAge(dateOfDiagnosis), dateOfDeathAge, physician);
//            results.add(trainingModelDTO);
//
//        }
        return results;
    }

    public List<ExportEntityDTO> getExportData( SearchOptionsDTO searchOptionsDTO) throws SQLException {
        if (!active) {
            LOG.error("CasandraEventsAccessor is not active");
        }
        List<ExportEntityDTO> results = new ArrayList<ExportEntityDTO>();
        String query;
        if (!getQueryToAppend(searchOptionsDTO).equals( ";")){
            query = "select name, identificationNumber, countryCode, mutation, locus, disorder, professionalExposure, professionalExposureTime gender, dateOfBirth," +
                    "dateOfDiagnosis,dateOfDeath,physician from Entries_Space.Entries where" + getQueryToAppend(searchOptionsDTO).substring(4);
        } else{
            query = "select name, identificationNumber, countryCode, mutation, locus, disorder, professionalExposure, professionalExposureTime gender, dateOfBirth," +
                    "dateOfDiagnosis,dateOfDeath,physician from Entries_Space.Entries;";
        }

        ResultSet resultSet = session.execute(query);
        Iterator<Row> iter = resultSet.iterator();
        while (iter.hasNext()) {
            if (resultSet.getAvailableWithoutFetching() == 100 && !resultSet.isFullyFetched())
                resultSet.fetchMoreResults();
            Row row = iter.next();
            String name = row.getString(0);
            String identificationNumber = row.getString(1);
            String countryCode = row.getString(2);
            String mutation = row.getString(3);
            String locus = row.getString(4);
            String disorder = row.getString(5);
            String professionalExposure = row.getString(6);
            Date professionalExposureTime = row.getDate(7);
            String gender = row.getString(8);
            Date dateOfBirth = row.getDate(9);
            Date dateOfDiagnosis = row.getDate(10);
            Date dateOfDeath = row.getDate(11);
            String physician = row.getString(12);
            int dateOfDeathAge = 100;
            if (dateOfDeath != null) {
                dateOfDeathAge = DateUtils.getAge(dateOfDeath);
            }
            ExportEntityDTO trainingModelDTO = new ExportEntityDTO(name, identificationNumber, countryCode, mutation, locus, disorder, professionalExposure, DateUtils.getAge(professionalExposureTime), gender, DateUtils.getAge(dateOfBirth), DateUtils.getAge(dateOfDiagnosis), dateOfDeathAge, physician);
            results.add(trainingModelDTO);

        }
        return results;
    }

    public long readMutationByCountryFiltered(String countryCode, SearchOptionsDTO searchOptionsDTO) {
        long no = 0;
        String query = "select count(*) from Entries_Space.Entries where countryCode = '" + countryCode + "'" + this.getQueryToAppend(searchOptionsDTO);
        ResultSet resultSet = session.execute(query);
        for (Row aResultSet : resultSet) {
            if (resultSet.getAvailableWithoutFetching() == 100 && !resultSet.isFullyFetched())
                resultSet.fetchMoreResults();
            no = aResultSet.getLong(0);
        }
        return no;
    }

    private String getQueryToAppend(SearchOptionsDTO searchOptionsDTO) {
        boolean noCondition = true;
        String queryChunk = "";
        if (!searchOptionsDTO.getMutation().equals("") && searchOptionsDTO.getMutation() != null) {
            queryChunk = queryChunk + " AND mutation = '" + searchOptionsDTO.getMutation() + "'";
            noCondition = false;
        }
        if (!searchOptionsDTO.getDisorder().equals("") && searchOptionsDTO.getDisorder() != null) {
            queryChunk = queryChunk + " AND disorder = '" + searchOptionsDTO.getDisorder() + "'";
            noCondition = false;
        }
        if (!searchOptionsDTO.getLocus().equals("") && searchOptionsDTO.getLocus() != null) {
            queryChunk = queryChunk + " AND locus = '" + searchOptionsDTO.getLocus() + "'";
            noCondition = false;
        }
        if (!searchOptionsDTO.getGender().equals("") && searchOptionsDTO.getGender() != null) {
            queryChunk = queryChunk + " AND gender = '" + searchOptionsDTO.getGender() + "'";
            noCondition = false;
        }
        if (!searchOptionsDTO.getProfessionalExposure().equals("") && searchOptionsDTO.getProfessionalExposure() != null) {
            queryChunk = queryChunk + " AND professionalExposure = '" + searchOptionsDTO.getProfessionalExposure() + "'";
            noCondition = false;
        }

        if ( searchOptionsDTO.getDateOfBirth() != null) {
            queryChunk = queryChunk + " AND dateOfBirth " + searchOptionsDTO.getDateOfBirthOperator() + " '" + searchOptionsDTO.getDateOfBirth() + "'";
            noCondition = false;
        }

        if ( searchOptionsDTO.getDateOfDiagnosis() != null) {
            queryChunk = queryChunk + " AND dateOfDiagnosis " + searchOptionsDTO.getDateOfDiagnosisOperator() + " '" + searchOptionsDTO.getDateOfDiagnosis() + "'";
            noCondition = false;
        }

        if ( searchOptionsDTO.getDateOfDeath() != null) {
            queryChunk = queryChunk + " AND dateOfDeath " + searchOptionsDTO.getDateOfDeathOperator() + " '" + searchOptionsDTO.getDateOfDeath() + "'";
            noCondition = false;
        }

        if (!searchOptionsDTO.getProfessionalExposureTime().equals("") && searchOptionsDTO.getProfessionalExposureTime() != null) {
            queryChunk = queryChunk + " AND professionaExposureTime <= '" + searchOptionsDTO.getProfessionalExposureTime() + "'";
            noCondition = false;
        }

        if (noCondition) {
            return ";";
        } else {
            return queryChunk + " ALLOW FILTERING ;";
        }
    }

    public List<BasicEntityDTO> readMutationByContry(String countryCode) throws SQLException {
        String query = "select mutation from Entries_Space.Entries where countryCode = '" + countryCode + "';";
        List<BasicEntityDTO> results = new ArrayList<BasicEntityDTO>();
        ResultSet resultSet = session.execute(query);
        Iterator<Row> iter = resultSet.iterator();
        while (iter.hasNext()) {
            if (resultSet.getAvailableWithoutFetching() == 100 && !resultSet.isFullyFetched())
                resultSet.fetchMoreResults();
            Row row = iter.next();
            BasicEntityDTO basicEntityDTO = new BasicEntityDTO(countryCode, 1);
            results.add(basicEntityDTO);
        }
        return results;
    }

    public long readMutationCount() throws SQLException {
        String query = "select count(*) from Entries_Space.Entries;";
        long no = 0;
        ResultSet resultSet = session.execute(query);
        Iterator<Row> iter = resultSet.iterator();
        while (iter.hasNext()) {
            if (resultSet.getAvailableWithoutFetching() == 100 && !resultSet.isFullyFetched())
                resultSet.fetchMoreResults();
            Row row = iter.next();
            ;
            no = row.getLong(0);
        }
        return no;
    }

    public boolean insertEntry(String query) throws SQLException {
        boolean success = true;
        try {
            session.execute(query);
        } catch (Exception e) {
            success = false;
        }
        return success;
    }

    public List<BasicEntityDTO> readMutationByGender(String gender) throws SQLException {
        String query = "select countryCode,mutation from Entries_Space.Entries where gender = '" + gender + "';";
        List<BasicEntityDTO> results = new ArrayList<BasicEntityDTO>();
        ResultSet resultSet = session.execute(query);
        Iterator<Row> iter = resultSet.iterator();
        while (iter.hasNext()) {
            if (resultSet.getAvailableWithoutFetching() == 100 && !resultSet.isFullyFetched())
                resultSet.fetchMoreResults();
            Row row = iter.next();
            String countryCode = row.getString(0);
            BasicEntityDTO basicEntityDTO = new BasicEntityDTO(countryCode, 1);
            results.add(basicEntityDTO);
        }
        return results;
    }

    public List<String> readDiagnostics() throws SQLException {
        if (!active) {
            LOG.error("CasandraEventsAccessor is not active");
        }
        List<String> results = new ArrayList<String>();
        ResultSet resultSet = session.execute("Select disorder from Entries_Space.Entries");
        Iterator<Row> iter = resultSet.iterator();
        while (iter.hasNext()) {
            if (resultSet.getAvailableWithoutFetching() == 100 && !resultSet.isFullyFetched())
                resultSet.fetchMoreResults();
            Row row = iter.next();
            String disorder = row.getString(0);
            results.add(disorder);
        }
        return results;
    }
}


