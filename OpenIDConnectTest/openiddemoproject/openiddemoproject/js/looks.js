$(':input[required]').parent().siblings('label').append($('<span>').text('*').addClass('requiredMarker'));

$('#logonLink').on('click', function(e) {
	$('#logonBox').modal({
		keyboard: false,
		backdrop: 'static'
	});
});

$('#doLogon').on('click', function(e) {
	alert('Thank you for Signing In');
	$('#logonBox').modal('hide');
});

$(document).ready(function () {
    $('[data-toggle="tooltip"]').tooltip();
    $('[data-toggle="popover"]').popover();

    $('#editor').wysiwyg();
});

function createNoty(type, message) {
    var html = '<div class="alert alert-' + type + ' alert-dismissable page-alert">';    
    html += '<button type="button" class="close"><span aria-hidden="true">×</span><span class="sr-only">Close</span></button>';
    html += message;
    html += '</div>';    
    $(html).hide().prependTo('#notify-holder').slideDown();
};

$(function(){
    var status = $("#status").val()
    var status_message = $("#status_message").val()

    if(status != undefined && status_message != undefined) {
        createNoty(status, status_message);
        $('.page-alert .close').click(function(e) {
            e.preventDefault();
            $(this).closest('.page-alert').slideUp();
        });
    }
});