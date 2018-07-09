from django.shortcuts import render, render_to_response, redirect
from django.http import HttpResponse, Http404, HttpResponseBadRequest, HttpResponseRedirect
from django.http.response import HttpResponseRedirect
from django.contrib.auth.decorators import login_required, permission_required

from django.contrib.staticfiles.templatetags.staticfiles import static
from django.contrib.staticfiles.storage import staticfiles_storage
from django.views.decorators.cache import never_cache

from django.contrib.auth import authenticate, login, logout
from django.contrib.auth.models import User
from django.core.paginator import Paginator, EmptyPage, PageNotAnInteger

from django.conf import settings
from django.contrib.auth.models import User, Group, Permission
from django.contrib.contenttypes.models import ContentType

from django.contrib.auth import authenticate

from openiddemoproject.forms import PatientForm
from openiddemoproject.models import Patient
from openiddemoproject.models import CurrentSession

import json
import os

import time

import requests
import base64
import json

lastAccessTime = 0
pinToTokenMap = {}
currentActivePIN = 0

def validatePIN(request):
    current_session = CurrentSession.objects.all()
    identity_server_url = "https://localhost:9444/oauth2/authorize?scope=openid%20pin&response_type=code&client_id=" + settings.CLIENT_ID + "&redirect_uri=http%3A%2F%2Flocalhost%3A8000%2Fopeniddemoproject%2Foauth2client/"

    if len(current_session) == 0:
        print("No sessions found. Trying to redirect")
        return redirect(identity_server_url)

    print("Validating the PIN " + request.POST.get('PIN'))
    pin_number = request.POST['PIN']
    accessToken = current_session[0].accessToken
    pin_in_session = current_session[0].currentPINNumber

    if pin_in_session != pin_number:
        print("The PIN entered doesn't exist")
        return render(request, 'promtTokenOrLogin.html', {
            'message': 'PIN entered is invalid or your session is expired. Try the correct pin or click login',
        })

    isValid = introspectToken(accessToken)
    if isValid == False:
        CurrentSession.objects.all().delete()
        return checkSession()
    else:
        current_session[0].lastAccessTime = int(time.time())
        current_session[0].save()
        return redirect(index)

def callUserInfoEp(token):
    print("About to call user info endpoint with token " + token)
    url = "https://localhost:9444/oauth2/userinfo?schema=openid%20pin"
    credential = "Bearer " + token

    headers = {
        "Authorization" : credential
    }

    response = requests.get(url, headers=headers, verify=False)
    json_response = response.json()

    print("User Info Response " + str(json_response))
    print("PIN Number = " + str(json_response["PIN"]))
    return json_response["PIN"]

def promptPIN(request):
    return render(request, 'promtTokenOrLogin.html', {
            'initial_message': 'Eneter the pin code or click the button to login',
        })

def checkSession(code=None):
    currentTime = int(time.time())

    accessToken = None
    pinNumber = None

    current_session = CurrentSession.objects.all()

    if code != None:
        print("A code exists. Thus trying to get the token")
        # This is the redirect from IDP. Now have to call the token endpoint
        accessToken = getToken(code)
        currentActivePIN = callUserInfoEp(accessToken)
        if len(current_session) > 0:
            CurrentSession.objects.all().delete()
        new_session = CurrentSession(currentPINNumber=currentActivePIN, accessToken=accessToken,
            lastAccessTime=int(time.time()))
        new_session.save()
        return (currentActivePIN, accessToken)

    identity_server_url = "https://localhost:9444/oauth2/authorize?scope=openid%20pin&response_type=code&client_id=" + settings.CLIENT_ID + "&redirect_uri=http%3A%2F%2Flocalhost%3A8000%2Fopeniddemoproject%2Foauth2client/"

    if len(current_session) == 0:
        print("No sessions found. Trying to redirect")
        return redirect(identity_server_url)
    if (currentTime - current_session[0].lastAccessTime) > 60:
        print("30s elapsed. Thus prompting the user for PIN number")
        return redirect(promptPIN)
    elif (currentTime - current_session[0].lastAccessTime) <= 60:
        print("30s didn't elapsed. Thus checking the validity of the token")
        if introspectToken(current_session[0].accessToken) == True:
            print("Token is still valid")
            current_session[0].lastAccessTime = int(time.time())
            current_session[0].save()
            return (current_session[0].currentPINNumber, current_session[0].accessToken)
        else:
            print("Token expired. Therefore, redirecting for login again.")
            current_session[0].delete()
            return redirect(identity_server_url)

def oauth2client(request):
    print("oauth2client endpoint is called")
    print("Authorization code = " + request.GET.get("code"))
    code = request.GET.get("code")

    pinAndTokenTupleOrRedirect = checkSession(code)

    return redirect(index)
    
def introspectToken(token):
    credential = "Basic " + base64.encodestring(b"admin:admin").decode('utf-8')[0:-1]
    headers = {
        "Authorization" : credential,
        "Content-Type" : "application/x-www-form-urlencoded"
    }

    data = "token=" + token
    url = "https://localhost:9444/oauth2/introspect"

    response = requests.post(url, data=data, headers=headers, verify=False)
    json_response = response.json()

    print("Introspect Response " + str(json_response))

    isValid = json_response['active']
    return isValid

def getToken(code):
    print("Get token method invoked")
    clientIDSecret = settings.CLIENT_ID + ":" + settings.CLIENT_SECRET
    credential = "Basic " + base64.encodestring(clientIDSecret.encode()).decode('utf-8')[0:-1]
    headers = {
        "Authorization" : credential,
        "Content-Type" : "application/x-www-form-urlencoded"
    }

    data = "grant_type=authorization_code&code=" + code + "&redirect_uri=http%3A%2F%2Flocalhost%3A8000%2Fopeniddemoproject%2Foauth2client/"
    url = "https://localhost:9444/oauth2/token"

    response = requests.post(url, data=data, headers=headers, verify=False)
    json_response = response.json()

    print("Response " + str(json_response))

    access_token = json_response['access_token']

    return access_token

def index(request):
    print("Going to check for sessions in the index")
    pinAndTokenTupleOrRedirect = checkSession()
    print("After checking sessions = " + str(type(pinAndTokenTupleOrRedirect)))
    print("Instance of = " + str(isinstance(pinAndTokenTupleOrRedirect, HttpResponseRedirect)))

    if isinstance(pinAndTokenTupleOrRedirect, HttpResponseRedirect):
        return pinAndTokenTupleOrRedirect

    return render(request, 'index.html', {
            'message': 'It is an awesome beginning',
        })

def patients(request):
    print("Going to check for sessions in the patients")
    pinAndTokenTupleOrRedirect = checkSession()
    print("After checking sessions = " + str(type(pinAndTokenTupleOrRedirect)))
    print("Instance of = " + str(isinstance(pinAndTokenTupleOrRedirect, HttpResponseRedirect)))

    if isinstance(pinAndTokenTupleOrRedirect, HttpResponseRedirect):
        return pinAndTokenTupleOrRedirect
        
    return render(request, 'patients.html', {
            'firstName': request.POST.get('firstName', ''),
            'lastName': request.POST.get('lastName', ''),
            'patientID': request.POST.get('patientID', ''),
        })

def getPatients(request):
    if request.method == "POST":
        firstName = request.POST.get('firstName', None)
        lastName = request.POST.get('lastName', None)
        patientID = request.POST.get('patientID', None)

        if(firstName == None and lastName == None and patientID == None):
            patients = Patient.objects.all()
        else:
            arguments = {}
            for queryParam, queryValue in request.POST.items():
                if queryValue != '':
                    arguments[queryParam + "__iexact"] = queryValue		# __iexact clause makes it case in-sensitive
            patients = Patient.objects.filter(**arguments)

        patientsDetail = []

        for patient in patients:
            gender = "Male" if patient.gender else "Female"
            patientDetails = [patient.firstName, patient.lastName, patient.patientID,
                                    patient.dateOfBirth, gender, patient.contactNumber]
            patientsDetail.append(patientDetails)

        jsonPatients = "{\"data\" : " + json.dumps(patientsDetail) + "}"
        response = HttpResponse(jsonPatients, content_type='application/json')

        return response
    raise Http404
