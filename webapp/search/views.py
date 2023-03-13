from django.shortcuts import render
from django.views.generic import ListView
from amazon.models import Product
from django.db.models import Q
# Create your views here.

class SearchProducView(ListView):
    template_name = "search/view.html"

    def get_context_data(self, *args, **kwargs):
        context=super(SearchProducView, self).get_context_data(*args, **kwargs)
        context['query'] = self.request.GET.get('q')
        return context

    def get_queryset(self, *args, **kwargs):
        request = self.request
        query = request.GET.get('q')
        print(query)
        if query is not None:
            return Product.objects.filter(Q(title__icontains=query) | Q(category__categoryName__icontains=query) | Q(description__icontains=query))
        return Product.objects.none()
        #capitalization does not matter
        #__iexact: field is exactly this
        #__icontains: field contains this


