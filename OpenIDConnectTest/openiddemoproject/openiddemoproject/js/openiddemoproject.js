$(function() {
	$("#dateOfBirth").datepicker({
		changeMonth: true,
		changeYear: true,
        yearRange: "-100:+0"
	});

	$("#pastDate").datepicker({
		changeMonth: true,
		changeYear: true,
        yearRange: "-10:+0",
        orientation: "top",
	});

    function readURL(input) {
        if (input.files && input.files[0]) {
            var reader = new FileReader();

            reader.onload = function (e) {
                $('#objectImg').attr('src', e.target.result);
                // $("#imgData").val(btoa(e.target.result));
            };

            reader.readAsDataURL(input.files[0]);
        }
    }

    $("#objectImage").change(function(){
        if(this.files[0].size/1024/1024 < 2) {
            readURL(this);
        } else{
            $("#objectImage").val("");
            showAlert(false, "Image is too large. Must be less than 2mb!");
        }
    });

    $('#symptoms').multiselect();
});

// Submit post on submit
$("#item-add").on('submit', function(event) {
	event.preventDefault();
	addItemPost();
});

// AJAX for posting
function addItemPost() {
    $.ajax({
    	url : "/itemAdd/",
    	type : "POST",

    	data : {
    		name : $('#name').val()
    	},

    	success : function(response) {
    		if(response) {
    			console.log(response);
    		}
    	},

    	error : function(xhr, errMsg, err) {
    		console.log("An error ocurred");
    	}
    });
}

// Following is for Django to set the CSRF header. May not be relevant to understand --------
$(function() {
    // This function gets cookie with a given name
    function getCookie(name) {
        var cookieValue = null;

        if (document.cookie && document.cookie != '') {
            var cookies = document.cookie.split(';');
            for (var i = 0; i < cookies.length; i++) {
                var cookie = jQuery.trim(cookies[i]);
                // Does this cookie string begin with the name we want?
                if (cookie.substring(0, name.length + 1) == (name + '=')) {
                    cookieValue = decodeURIComponent(cookie.substring(name.length + 1));
                    break;
                }
            }
        }

        return cookieValue;
    }

    var csrftoken = getCookie('csrftoken');

	/*
	The functions below will create a header with csrftoken
	*/
	function csrfSafeMethod(method) {
		// these HTTP methods do not require CSRF protection
		return (/^(GET|HEAD|OPTIONS|TRACE)$/.test(method));
	}

    function sameOrigin(url) {
		// test that a given url is a same-origin URL
		// url could be relative or scheme relative or absolute
		var host = document.location.host; // host + port
		var protocol = document.location.protocol;
		var sr_origin = '//' + host;
		var origin = protocol + sr_origin;
		// Allow absolute or scheme relative URLs to same origin
		return (url == origin || url.slice(0, origin.length + 1) == origin + '/') ||
		    (url == sr_origin || url.slice(0, sr_origin.length + 1) == sr_origin + '/') ||
		    // or any other URL that isn't scheme relative or absolute i.e relative.
		    !(/^(\/\/|http:|https:).*/.test(url));
	}

	$.ajaxSetup({
		beforeSend: function(xhr, settings) {
			if (!csrfSafeMethod(settings.type) && sameOrigin(settings.url)) {
				// Send the token to same-origin, relative URLs only.
				// Send the token only if the method warrants CSRF protection
				// Using the CSRFToken value acquired earlier
				xhr.setRequestHeader("X-CSRFToken", csrftoken);
			}
		}
	});
});

$('#confirmDelete').on('show.bs.modal', function (e) {
    $message = $(e.relatedTarget).attr('data-message');
    var targetUrl = $(e.relatedTarget).attr('target-url');
    var redirectUrl = $(e.relatedTarget).attr('redirect-url');
    $(this).find('.modal-body p').text($message);
    $title = $(e.relatedTarget).attr('data-title');
    $(this).find('.modal-title').text($title);
    var form = $(e.relatedTarget).closest('form');
    $(this).find('.modal-footer #confirm').data('url', targetUrl);
    $(this).find('.modal-footer #confirm').data('redirectUrl', redirectUrl);
});

$('#confirmDelete').find('.modal-footer #confirm').on('click', function() {
    var url = $(this).data('url');
    var redirectUrl = $(this).data('redirectUrl');

    $('#confirmDelete').modal('hide');
    $.ajax({
        url: url,
        type:"POST",

        success: function(data) {
            window.location = redirectUrl;
        }
    })  
});