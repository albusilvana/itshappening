package com.web.api.rest;

import com.DTO.BasicEntityDTO;
import com.DTO.EnhancedBasicEntityDTO;
import com.DTO.EntryDTO;
import com.DTO.EntryFromUIDTO;
import com.Service.EntryService;
import com.google.inject.Inject;
import org.apache.poi.ss.usermodel.Workbook;

import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

/**
 * Created by silvana.albert on 4/13/15.
 */
@Path("/entries")
public class EntryEndpoint {
    private static EntryService entryService;

    @Inject
    public EntryEndpoint(EntryService entryService) {
        this.entryService = entryService;
    }

    @GET
    @Path("count/all")
    @Produces(MediaType.APPLICATION_JSON)
    public List<BasicEntityDTO> getCountPerCountry(@QueryParam("token") String token) throws Exception {
        if (token != "") {
            List<BasicEntityDTO> entryDTOList = entryService.getAllBasicEntitiesDTO();
            return entryDTOList;
        } else return null;
    }

    @GET
    @Path("count/enhanced/all")
    @Produces(MediaType.APPLICATION_JSON)
    public List<EnhancedBasicEntityDTO> getEnhancedCountPerCountry(@QueryParam("token") String token) throws Exception {
        if (token != "") {
            List<EnhancedBasicEntityDTO> entryDTOList = entryService.getAllEnhancedEntitiesDTO();
            return entryDTOList;
        } else return null;
    }

    @GET
    @Path("count/enhanced/byGender")
    @Produces(MediaType.APPLICATION_JSON)
    public List<EnhancedBasicEntityDTO> getEnhancedCountPerCountryByGender(@QueryParam("gender") String gender, @QueryParam("token") String token) throws Exception {
        if (token != "") {
            List<EnhancedBasicEntityDTO> entryDTOList = entryService.getEnhancedBasicEntitiesDtoByGender(gender);
            return entryDTOList;
        } else return null;
    }

    @GET
    @Path("count/byGender")
    @Produces(MediaType.APPLICATION_JSON)
    public List<BasicEntityDTO> getCountPerCountryByGender(@QueryParam("gender") String gender, @QueryParam("token") String token) throws Exception {
        if (token != "") {
            List<BasicEntityDTO> entryDTOList = entryService.getAllEntitiesByGender(gender);
            return entryDTOList;
        } else {
            return null;
        }
    }

    @GET
    @Path("total/all")
    @Produces(MediaType.APPLICATION_JSON)
    public long getTotalCount(@QueryParam("token") String token) throws Exception {
        if (token != "") {
            long no = entryService.getMutationCount();
            return no;
        } else {
            return 0;
        }
    }

    @POST
    @Path("create")
    @Consumes(MediaType.APPLICATION_JSON)
    public String createEntry(EntryFromUIDTO entryFromUIDTO, @QueryParam("token") String token) throws Exception {
        String resp = entryService.insertEntry(entryFromUIDTO.getName(), entryFromUIDTO.getIdentificationNumber(),
                entryFromUIDTO.getCountryCode(), entryFromUIDTO.getDateOfBirth(), entryFromUIDTO.getDateOfDiagnosis(),
                entryFromUIDTO.getDateOfDeath(), entryFromUIDTO.getGender(), entryFromUIDTO.getProfessionalExposure(),
                entryFromUIDTO.getProfessionalExposureTime(),
                entryFromUIDTO.getDetails(), entryFromUIDTO.getMutation(), entryFromUIDTO.getLocus(),
                entryFromUIDTO.getDisorder(), entryFromUIDTO.getPhysician());
        return resp;
    }

    @POST
    @Path("filtered")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public List<BasicEntityDTO> getFilteredEntries(com.DTO.SearchOptionsDTO searchOptionsDTO) throws Exception {
        List<BasicEntityDTO>  resp = entryService.getEntitiesFiltered(searchOptionsDTO);
        return resp;
    }

    @POST
    @Path("export/csv")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response exportToCsv(com.DTO.SearchOptionsDTO searchOptionsDTO) throws Exception {
        Workbook workbook = entryService.getCSVExportLocation(searchOptionsDTO);
        return Response.ok(workbook).header("Content-Disposition", "attachment; filename=" + "Mutations.xls").build();
    }

    @POST
    @Path("export/pdf")
    @Consumes(MediaType.APPLICATION_JSON)
    public javax.ws.rs.core.Response exportToPdf(com.DTO.SearchOptionsDTO searchOptionsDTO) throws Exception {
        Workbook workbook = entryService.getCSVExportLocation(searchOptionsDTO);

        String FILE_PATH = "/Users/salbu/Desktop/itshappening/GeneHive/mutations.xls";

        File file = new File(FILE_PATH);

        Response.ResponseBuilder response = Response.ok((Object) file);
        response.header("Content-Disposition",
                "attachment; filename=new-excel-file.xls");
        return response.build();
    }


    private StreamingOutput getOut(final byte[] excelBytes) {
        return new StreamingOutput() {
            @Override
            public void write(OutputStream out) throws IOException, WebApplicationException {
                out.write(excelBytes);
            }
        };
    }
}
