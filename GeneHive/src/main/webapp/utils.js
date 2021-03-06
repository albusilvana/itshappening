$(document).ready(function () {
    // if (!localStorage.getItem("isUserLoggedIn")) {
    //     localStorage.setItem("errorMessage", "You have to log in in order to access this page.");
    //     window.location = "http://localhost:9095/hh/login.html";
    // }
    showStatistics();
    $.ajax({
        url: "http://localhost:9095/hh/API/v1/entries/total/all?token=dsf"
    }).then(function (data) {
        $("#currentEntriesCount").text(data);
    });
    $("#allChecked").prop("checked", true)
    insertProfesionalExposure();
    insertOperators();
    refreshWithAll();
    $("#loadingSearch").hide();
    $("#loadingPredict").hide();
    $("#loadingInsert").hide();
    $("#container").css({"margin": "0px auto 0 500px"});
});
function redirectToLogin(){
    window.location = "http://localhost:9095/hh/login.html";
}
function insertProfesionalExposure() {
    var exposures = ",Arsenic,Asbestos,Asphalt fumes,Benzene,Beryllium,1-Bromopropane,13-Butadiene,Cadmium,Chromium,Diacetyl,Diesel exhaust,Ethylene oxide,Formaldehyde,Hexavalent chromium,Hydrogen sulfide,Isocyanates,Lead,Mercury,Metals toxic,Metalworking fluids,Methylene chloride,SilicaCrystalline,Solvents,Synthetic mineral fibers,Toluene".split(",");
    for (var i = 0; i < exposures.length; i++) {
        $("#insertProfestionalExposure").append($("<option />").val(exposures[i]).text(exposures[i]));
        $("#profestionalExposure").append($("<option />").val(exposures[i]).text(exposures[i]));

    }
}

function insertOperators() {
    var operators = "<,>,<=,>=,=".split(",");
    for (var i = 0; i < operators.length; i++) {
        $("#operatorBirthDate").append($("<option />").val(operators[i]).text(operators[i]));
        $("#operatorDiagnosisDate").append($("<option />").val(operators[i]).text(operators[i]));
        $("#operatorDeathDate").append($("<option />").val(operators[i]).text(operators[i]));
    }
    var profExposure = $("#professionalExposureOperator");
    profExposure.append($("<option />").val(1).text("Under 1 Year"));
    profExposure.append($("<option />").val(5).text("Between 1 and 5 years"));
    profExposure.append($("<option />").val(10).text("Between 5 and 10 years"));
    profExposure.append($("<option />").val(20).text("Between 10 and 20 years"));
    profExposure.append($("<option />").val(25).text("Over 20 years"));
}
function showInsert() {
    $("#insertContent").show();
    $("#statisticsContent").hide()
    $("#homeContent").hide();
    $("#homeLi").removeClass("active");
    $("#statsLi").removeClass("active");
    $("#insertLi").addClass("active");
    insertProfesionalExposure();
}

function showHome() {

    $("#homeContent").show();
    $("#statisticsContent").hide();
    $("#insertContent").hide();
    $("#homeLi").addClass("active");
    $("#statsLi").removeClass("active");
    $("#insertLi").removeClass("active");

}
function showStatistics() {
    $("#homeContent").hide();
    $("#statisticsContent").show();
    $("#insertContent").hide();
    $("#homeLi").removeClass("active");
    $("#statsLi").addClass("active");
    $("#insertLi").removeClass("active");
}

function refreshWithAll() {
    resetFilters();
    var selectedVal = $('input[name=mapType]:checked').val();
    if (selectedVal == "cluster") {
        var data = $.getJSON('http://localhost:9095/hh/API/v1/entries/count/all?token=' + localStorage.getItem("token"), function (data) {
            loadClusterData(data);
        });
    } else {
        var data = $.getJSON('http://localhost:9095/hh/API/v1/entries/count/enhanced/all?token=' + localStorage.getItem("token"), function (data) {
            loadHighLightData(data);
        });
    }
}

function refreshMapWithPrediction() {
    $("#loadingPredict").show();
    var selectedVal = $('input[name=mapType]:checked').val();
    var bdate = new Date($("#predDay").val());
    var bmilliseconds = bdate.getTime();
    var url;
    if (selectedVal == "cluster") {
        url = "http://localhost:9095/hh/API/v1/entries/predicted?dateOfPrediction=" + bmilliseconds;
    } else {
        url = "http://localhost:9095/hh/API/v1/entries/predicted/highlight?dateOfPrediction=" + bmilliseconds;
    }
    $.ajax({
        contentType: 'application/json',
        data: JSON.stringify(computeSearchJsonData()),
        dataType: 'json',
        success: function (data) {
            if (selectedVal == "cluster") {
                loadClusterData(data);
            } else {
                loadHighLightData(data);
            }
            $("#loadingPredict").hide();
        },
        error: function () {
        },
        processData: false,
        type: 'POST',
        url: url
    });
}
function resetFilters(){
    $("#birthDay").val("");
    $("#diagnosticDay").val("");
    $("#deathDay").val("");
    $("#profestionalExposure").val("");
    $("#geneName").val("");
    $("#locus").val("");
    $("#disprderName").val("")
}
function computeSearchJsonData() {
    var jsonData = {};

    var bdate = new Date($("#birthDay").val());
    var bmilliseconds = bdate.getTime();

    var ddate = new Date($("#diagnosticDay").val());
    var dmilliseconds = ddate.getTime();

    var dedate = new Date($("#deathDay").val());
    var demilliseconds = dedate.getTime();

    jsonData.dateOfBirthOperator = $("#operatorBirthDate").val();
    jsonData.dateOfDiagnosisOperator = $("#operatorDiagnosisDate").val();
    jsonData.dateOfDeathOperator = $("#operatorDeathDate").val();
    jsonData.dateOfBirth = bmilliseconds;
    jsonData.dateOfDiagnosis = dmilliseconds;
    jsonData.dateOfDeath = demilliseconds;
    jsonData.gender = $('input[name=sex]:checked').val();
    jsonData.professionalExposure = $("#profestionalExposure").val();
    jsonData.professionalExposureTime = parseInt($("#professionalExposureOperator").val());
    jsonData.mutation = $("#geneName").val();
    jsonData.locus = $("#locus").val();
    jsonData.disorder = $("#disprderName").val();
    console.log(jsonData);
    return jsonData;
}
function getResults() {
    $("#loadingSearch").show();
    var selectedVal = $('input[name=mapType]:checked').val();
    var url = "";
    if (selectedVal == "cluster") {
        url = "http://localhost:9095/hh/API/v1/entries/filtered";
    } else {
        url = "http://localhost:9095/hh/API/v1/entries/filtered/highlight";
    }
    $.ajax({
        contentType: 'application/json',
        data: JSON.stringify(computeSearchJsonData()),
        dataType: 'json',
        success: function (data) {
            if (selectedVal == "cluster") {
                loadClusterData(data);
            } else {
                loadHighLightData(data);
            }
            $("#loadingSearch").hide();
        },
        error: function () {

        },
        processData: false,
        type: 'POST',
        url: url
    });
}
$(document).on("submit", "form.fileDownloadForm", function (e) {
    var bdate = new Date($("#birthDay").val());
    var bmilliseconds = bdate.getTime();

    var ddate = new Date($("#diagnosticDay").val());
    var dmilliseconds = ddate.getTime();

    var dedate = new Date($("#deathDay").val());
    var demilliseconds = dedate.getTime();

    var url = "http://localhost:9095/hh/API/v1/entries/export/csv?dateOfBirthOperator='" + encodeURIComponent($("#operatorBirthDate").val()) +
        "'&dateOfDiagnosisOperator='" + encodeURIComponent($("#operatorDiagnosisDate").val()) + "'&dateOfDeathOperator=" + encodeURIComponent($("#operatorDeathDate").val())
        + "&dateOfBirth=" + bmilliseconds + "&dateOfDiagnosis=" + dmilliseconds + "&dateOfDeath=" + demilliseconds + "&gender=" +
        $('input[name=sex]:checked').val() + "&professionalExposure=" + $("#profestionalExposure").val() + "&professionalExposureTime=" +
        parseInt($("#professionalExposureOperator").val()) + "&mutation=" + $("#geneName").val() + "&locus=" + $("#locus").val() +
        "&disorder=" + $("#disprderName").val();
    $.fileDownload(url, {
        preparingMessageHtml: "We are preparing your report, please wait...",
        failMessageHtml: "There was a problem generating your report, please try again.",
        httpMethod: "GET"
    });
    e.preventDefault(); //otherwise a normal form submit would occur
});
$(document).on("submit", "form.fileDownloadFormPdf", function (e) {
    var bdate = new Date($("#birthDay").val());
    var bmilliseconds = bdate.getTime();

    var ddate = new Date($("#diagnosticDay").val());
    var dmilliseconds = ddate.getTime();

    var dedate = new Date($("#deathDay").val());
    var demilliseconds = dedate.getTime();

    var url = "http://localhost:9095/hh/API/v1/entries/export/pdf?dateOfBirthOperator='" + encodeURIComponent($("#operatorBirthDate").val()) +
        "'&dateOfDiagnosisOperator='" + encodeURIComponent($("#operatorDiagnosisDate").val()) + "'&dateOfDeathOperator=" + encodeURIComponent($("#operatorDeathDate").val())
        + "&dateOfBirth=" + bmilliseconds + "&dateOfDiagnosis=" + dmilliseconds + "&dateOfDeath=" + demilliseconds + "&gender=" +
        $('input[name=sex]:checked').val() + "&professionalExposure=" + $("#profestionalExposure").val() + "&professionalExposureTime=" +
        parseInt($("#professionalExposureOperator").val()) + "&mutation=" + $("#geneName").val() + "&locus=" + $("#locus").val() +
        "&disorder=" + $("#disprderName").val();
    $.fileDownload(url, {
        preparingMessageHtml: "We are preparing your report, please wait...",
        failMessageHtml: "There was a problem generating your report, please try again.",
        httpMethod: "GET"
    });
    e.preventDefault(); //otherwise a normal form submit would occur
});

function saveEntry() {
    $("#loadingInsert").show();
    var jsonData = {};

    var bdate = new Date($("#insertBirthDate").val());
    var bmilliseconds = bdate.getTime();

    var ddate = new Date($("#insertDiagnosisDate").val());
    var dmilliseconds = ddate.getTime();

    var dedate = new Date($("#insertDeathDate").val());
    var demilliseconds = dedate.getTime();


    jsonData.name = "'" + $("#insertName").val() + "'";
    jsonData.identificationNumber = "'" + $("#insertIdentificationNumber").val() + "'";
    jsonData.countryCode = "'" + $("#insertCountry").val() + "'";
    jsonData.dateOfBirth = bmilliseconds;
    jsonData.dateOfDiagnosis = dmilliseconds;
    jsonData.dateOfDeath = demilliseconds;
    jsonData.gender = "'" + $("#insertGender").val() + "'";
    jsonData.professionalExposure = "'" + $("#insertProfestionalExposure").val() + "'";
    jsonData.professionalExposureTime = $("#insertProfessionalExposureTime").val();
    jsonData.details = "'" + $("#insertDetails").val() + "'";
    jsonData.mutation = "'" + $("#insertMutation").val() + "'";
    jsonData.locus = "'" + $("#insertLocus").val() + "'";
    jsonData.disorder = "'" + $("#insertDisorder").val() + "'";
    jsonData.physician = "'" + $("#insertPhisician").val() + "'";

    $("#loadingInsert").hide();
    $.ajax({
        contentType: 'application/json',
        data: JSON.stringify(jsonData),
        dataType: 'json',
        success:  alert("You have successfully created a new entry."),
//            console.log(data);
//            $("#loadingInsert").hide();
//
//        },
//        error: function () {
//        },
        processData: false,
        type: 'POST',
        url: "http://localhost:9095/hh/API/v1/entries/create"
    })
}

function loadHighLightData(data) {


    // Initiate the chart
    $('#container').highcharts('Map', {

        title: {
            text: 'Entries by country'
        },

        legend: {
            title: {
                text: 'Mutations number',
                style: {
                    color: (Highcharts.theme && Highcharts.theme.textColor) || 'black'
                }
            }
        },

        mapNavigation: {
            enabled: true,
            buttonOptions: {
                verticalAlign: 'bottom'
            }
        },

        tooltip: {
            backgroundColor: 'none',
            borderWidth: 0,
            shadow: false,
            useHTML: true,
            padding: 0,
            pointFormat: '<span class="f32"><span class="flag {point.flag}"></span></span>'
                + ' {point.name}: <b>{point.value}</b>',
            positioner: function () {
                return { x: 0, y: 250 };
            }
        },

        colorAxis: {
            min: 1,
            max: 1000,
            type: 'logarithmic'
        },

        series: [
            {
                data: data,
                mapData: Highcharts.maps['custom/world'],
                joinBy: ['iso-a2', 'code'],
                name: 'Mutation count',
                states: {
                    hover: {
                        color: '#BADA55'
                    }
                }
            }
        ]
    });
}
function loadClusterData(data) {
    var mapData = Highcharts.geojson(Highcharts.maps['custom/world']);

    // Correct UK to GB in data
    $.each(data, function () {
        if (this.code === 'UK') {
            this.code = 'GB';
        }
    });

    $('#container').highcharts('Map', {
        chart: {
            borderWidth: 1
        },

        title: {
            text: 'Entries by country'
        },

        subtitle: {
            text: 'Click to see more details'
        },

        legend: {
            enabled: false
        },

        mapNavigation: {
            enabled: true,
            buttonOptions: {
                verticalAlign: 'bottom'
            }
        },

        series: [
            {
                name: 'Countries',
                mapData: mapData,
                enabled:true,
                color: '#2419e0',
                enableMouseTracking: false,
                format: '{point.value}'
            },
            {
                type: 'mapbubble',
                mapData: mapData,
                name: 'Mutation count',
                joinBy: ['iso-a2', 'code'],
                data: data,
                minSize: 0,
                maxSize: 100,
                tooltip: {
                    pointFormat: '{point.code}: {point.z} '
                }
            }
        ]
    });

    $("#container").css({"margin": "0px auto 0 500px"});
}
