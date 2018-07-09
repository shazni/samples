from django import forms
import datetime

GENDER_CHOICES = (('Male', 'Male'), ('Female', 'Female'))

class PatientForm(forms.Form):
    firstName = forms.CharField(initial='')
    lastName = forms.CharField(required=False, initial='')
    patientID = forms.CharField(initial='')
    address = forms.CharField(required=False, initial='')
    dateOfBirth = forms.DateField(initial='')
    gender = forms.ChoiceField(widget=forms.RadioSelect, choices=GENDER_CHOICES)
    contactNumber = forms.CharField(required=False, initial='')

    def clean_patientID(self):
        patientID = self.cleaned_data['patientID']
        if not patientID.isalnum():
            raise forms.ValidationError("id should be alphanumeric")
        return patientID