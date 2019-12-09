from django.db import models

class Patient(models.Model):
    firstName = models.CharField(max_length=20)
    lastName = models.CharField(max_length=20, blank=True, null=True)
    patientID = models.CharField(max_length=20, primary_key=True)
    address = models.CharField(max_length=80, blank=True, null=True)
    dateOfBirth = models.DateField()
    gender = models.BooleanField(default=True)
    contactNumber = models.CharField(max_length=20, blank=True, null=True)

    def __str__(self):
        return '%s %s' % (self.firstName, self.lastName)

    class Meta:
        ordering = ['firstName']

class CurrentSession(models.Model):
    currentPINNumber = models.CharField(max_length=10)
    accessToken = models.CharField(max_length=40)
    lastAccessTime = models.IntegerField()
    refreshToken = models.CharField(max_length=40)
    id_token = models.TextField()
    username = models.CharField(max_length=10)
