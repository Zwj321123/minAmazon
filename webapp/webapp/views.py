from django.contrib.auth import authenticate, login, get_user_model
from django.shortcuts import render, redirect
from django.http import HttpResponse
from .forms import ContactForm

def home_page(request):
    #print(request.session.get("first_name", "Unknown")) #get
    context = {
        "title":"Welcome!",
        "content":"Welcome to the homepage.",
    }
    if request.user.is_authenticated:
        context["premium_content"] = "YEAHHHH"
    return render(request, "home_page.html", context)

def about_page(request):
    context = {
        "title":"about page",
        "content":"Welcome to the about page."
    }
    return render(request, "home_page.html", context)

def contact_page(request):
    contact_form = ContactForm(request.POST or None)
    context = {
        "title": "contact",
        "content": "Welcome to the contact page.",
        "form": contact_form,
    }
    if contact_form.is_valid():
        print(contact_form.cleaned_data)
    # if request.method == "POST":
    #     print(request.POST)
    #     print(request.POST.get('fullname'))
    #     print(request.POST.get('email'))
    #     print(request.POST.get('content'))
    return render(request, "contact/view.html", context)

