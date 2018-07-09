var patientsGrid;

$(document).ready(function () {
	$('.btn-success').on('click', function (e) {
		alert('Hello');
	});

	if(typeof patientsGrid == 'undefined') {
	    patientsGrid = initializePatientsGrid();
	} else {
		patientsGrid.destroy();
		patientsGrid = initializePatientsGrid();
	}
});

function initializePatientsGrid() {
	var context_prefix = $("#context_prefix").val();
	var url = "/" + context_prefix + "getPatients/";

	return $('#patients-data-table').DataTable({
		"ajax" : {
			"url" : url,
            "data" : function(d) {
                d.firstName = $("#firstName").val(),
                d.lastName = $("#lastName").val(),
                d.patientID = $("#patientID").val()
            },
			"method": "POST"
		},
		"lengthMenu": [5, 10, 15, 20, 25],
        "searching": false ,
        "responsive": false,
		"columnDefs": [
			{ "width": "10%", "targets": 0 },
			{ "width": "10%", "targets": 1 },
			{ "width": "10%", "targets": 2 },
			{ "width": "15%", "targets": 3 },
            { "width": "15%", "targets": 4 },
			{ "width": "15%", "targets": 5 },
			{ "width": "20%", "targets": 6 ,
				"data": null,
				"createdCell": function (td, cellData, rowData, row, col) {
					$(td).html('<button onclick="window.location.href=\'' + "/" + context_prefix + 'patientEdit/' + rowData[2] + '/\'" ' +
						'class="btn btn-xs btn-info" type="button">Edit</button>' +

					    ' <button onclick="window.location.href=\'' + "/" + context_prefix + 'patient/' + rowData[2] + '/\'" class="btn btn-xs btn-success"' +
					    ' type="button">Details</button>' +

					    ' <form style="display:inline">' + 
					    '<button class="btn btn-xs btn-danger" type="button" data-toggle="modal"' +
					    ' data-target="#confirmDelete" data-title="Delete Patient" ' + 
					    ' target-url="/' + context_prefix + 'deletePatient/' + rowData[2] +'/"' +
					    ' redirect-url="/' + context_prefix + 'patients/"' +
					    'data-message="Are you sure you want to delete this patient ?">' +
					    '<i class="glyphicon glyphicon-trash"></i> Delete</button></form>'

					    );
				}
			}
		]
	});
}