$(document).ready(function () {

    $("#btnSubmit").click(function (event) {

        //stop submit the form, we will post it manually.
        event.preventDefault();

        index();

    });

    $("#btnSearch").click(function (event) {

        //stop submit the form, we will post it manually.
        event.preventDefault();

        searchQuery();

    });

});

function index() {

    // Get form
    const form = $('#indexForm')[0];

    const data = new FormData(form);

    $("#btnSubmit").prop("disabled", true);

    $.ajax({
        type: "POST",
        enctype: 'multipart/form-data',
        url: "/index",
        data: data,
        processData: false, //prevent jQuery from automatically transforming the data into a query string
        contentType: false,
        cache: false,
        timeout: 600000,
        success: function (data) {
            $('#result').empty();
            $("#result").text(data);
            $("#btnSubmit").prop("disabled", false);

        },
        error: function (e) {
            $('#result').empty();
            $("#result").text(e.responseText);
            $("#btnSubmit").prop("disabled", false);

        }
    });

}

function searchQuery() {
    const data = []
    const elements = $('#searchForm div')

    for (let i = 0; i < elements.length; i++) {
        const element = elements[i]
        let input = $(element).children('input')[0]
        let operation = $(element).children('select')[0]
        let value = input.value.trim();
        if (value.replace(/"/g, '').trim()) {
            data.push({
                field: input.name,
                value: input.value,
                operation: operation.value
            })
        }
    }

    $("#btnSearch").prop("disabled", true);

    $.ajax({
        type: "POST",
        url: "/search/query",
        data: JSON.stringify(data),
        contentType: 'application/json',
        success: function (data) {
            appendResults(data);
            $("#btnSearch").prop("disabled", false);

        },
        error: function (e) {
            $('#result').empty();
            $("#result").text(e.responseText);
            $("#btnSearch").prop("disabled", false);
        }
    });
}

function appendResults(data) {
    $('#result').empty();
    for (let i = 0; i < data.length; i++) {
        const result = data[i]
        $.each(result, function (key, value) {
            $('#result').append('<li>' + key + ': ' + value + '</li>');
        });
        const moreLikeThisButton = $('<button/>', {
            text: 'More Like This',
            click: function () {
                searchMoreLikeThis(this, result.filename)
            }
        });
        const geoDistanceButton = $('<button/>', {
            text: 'Geo Distance',
            click: function () {
                searchGeoDistance(this, result.filename)
            }
        });
        const downloadButton = $('<button/>', {
            text: 'Download',
            click: function () {
                downloadFile(this, result.filename)
            }
        });
        $('#result').append('<br/>');
        $('#result').append(moreLikeThisButton);
        $('#result').append(geoDistanceButton);
        $('#result').append(downloadButton);
        $('#result').append('<hr/>');
    }
}

function searchMoreLikeThis(element, filename) {
    $(element).prop("disabled", true);

    $.ajax({
        type: "GET",
        url: "/search/more-like-this/" + filename,
        success: function (data) {
            appendResults(data);
            $(element).prop("disabled", false);

        },
        error: function (e) {
            $('#result').empty();
            $("#result").text(e.responseText);
            $(element).prop("disabled", false);
        }
    });
}

function searchGeoDistance(element, filename) {
    $(element).prop("disabled", true);

    $.ajax({
        type: "GET",
        url: "/search/geo-distance/" + filename,
        success: function (data) {
            $('#result').empty();
            for (let i = 0; i < data.length; i++) {
                const result = data[i]
                $.each(result, function (key, value) {
                    $('#result').append('<li>' + key + ': ' + value + '</li>');
                });
                $('#result').append('<hr/>');
            }
            $(element).prop("disabled", false);

        },
        error: function (e) {
            $('#result').empty();
            $("#result").text(e.responseText);
            $(element).prop("disabled", false);
        }
    });
}

function downloadFile(element, filename) {
    $(element).prop("disabled", true);

    $.ajax({
        type: "GET",
        url: "/download/" + filename,
        success: function (data) {
            const binaryData = [];
            binaryData.push(data);
            const url = window.URL.createObjectURL(new Blob(binaryData, {type: "application/pdf"}));
            const link = document.createElement('a');
            link.href = url;
            link.download = filename;
            link.click();
            window.URL.revokeObjectURL(url);
            $(element).prop("disabled", false);

        },
        error: function (e) {
            $('#result').empty();
            $("#result").text(e.responseText);
            $(element).prop("disabled", false);
        }
    });
}
