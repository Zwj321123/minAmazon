from django.contrib.auth import authenticate, login, get_user_model
from django.shortcuts import render, redirect
from django.contrib import messages
from .forms import RegisterForm, GuestForm, UserRegisterForm
from .models import GuestEmail

# Create your views here.

def guest_login_view(request):
    form = GuestForm(request.POST or None)
    context = {
        "form": form
    }
    print("User logged in")
    #print(request.user.is_authenticated)
    next_ = request.GET.get('next')
    next_post = request.POST.get('next')
    redirect_path = next_ or next_post or None
    if form.is_valid():
        email = form.Cleaned_data.get("email")
        new_guest_email = GuestEmail.objects.create(email=email)
        request.session['guest_email_id'] = new_guest_email.id
    return redirect("/register/")



# def login_page(request):
#     form = LoginForm(request.POST or None)
#     context = {
#         "form": form
#     }
#     print("User logged in")
#     #print(request.user.is_authenticated)
#     next_ = request.GET.get('next')
#     next_post = request.POST.get('next')
#     redirect_path = next_ or next_post or None
#     if form.is_valid():
#         print(form.cleaned_data)
#         username = form.cleaned_data.get("username")
#         password = form.cleaned_data.get("password")
#         user = authenticate(request, username=username, password=password)
#         if user is not None:
#             #print(request.user.is_authenticated)
#             login(request, user)
#             if request.method == 'GET' or request.method == 'POST':
#                 return redirect(redirect_path)
#             else:
#                 return redirect("/")
#         else:
#             # Return an 'invalid login' error message.
#             print("Error")
#     return render(request, "accounts/login.html", context)

def register_page(request):
    if request.method == 'POST':
        form = UserRegisterForm(request.POST)
        if form.is_valid():
            form.save()
            username = form.cleaned_data.get('username')
            messages.success(request, f'Account created for {username}!')
            return redirect('login')
    else:
        form = UserRegisterForm()
    return render(request, 'accounts/register.html', {'form': form})


# User = get_user_model()
# def register_page(request):
#     form = RegisterForm(request.POST or None)
#     context = {
#         "form": form
#     }
#     if form.is_valid():
#         print(form.cleaned_data)
#         username = form.cleaned_data.get("username")
#         email = form.cleaned_data.get("email")
#         password = form.cleaned_data.get("password")
#         new_user = User.objects.create_user(username, email, password)
#         print(new_user)
#         messages.success(request, f'Your account has been created! You are now able to login')
#         return redirect("login")
#
#     return render(request, "accounts/register.html", context)
