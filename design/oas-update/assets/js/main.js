

var $ = jQuery.noConflict();

jQuery( document ).ready( function( $ ) { 

	"use strict";
	 
	// Menu Trigger
	$('#menuToggle').on('click', function(event) {
		var windowWidth = $(window).width();   		 
		if (windowWidth<1010) { 
			$('body').removeClass('open'); 
			if (windowWidth<760){ 
				$('#left-panel').slideToggle(); 
			} else {
				$('#left-panel').toggleClass('open-menu');  
			} 
		} else {
			$('body').toggleClass('open');
			$('#left-panel').removeClass('open-menu');  
		} 
			 
	}); 

	 
	$(".menu-item-has-children.dropdown").each(function() {
		$(this).on('click', function() {
			var $temp_text = $(this).children('.dropdown-toggle').html();
			$(this).children('.sub-menu').prepend('<li class="subtitle">' + $temp_text + '</li>'); 
		});
	});


	// Load Resize 
	$(window).on("load resize", function(event) { 
		var windowWidth = $(window).width();  		 
		if (windowWidth<1010) {
			$('body').addClass('small-device'); 
		} else {
			$('body').removeClass('small-device');  
		} 
		
	});



	//Enable check and uncheck all functionality
    $('.checkbox-toggle').click(function () {
      	var clicks = $(this).data('clicks')
      	if (clicks) {
        	//Uncheck all checkboxes
        	$('.mailbox-messages input[type=\'checkbox\']').prop('checked', false)
        	$('.checkbox-toggle .far.fa-check-square').removeClass('fa-check-square').addClass('fa-square')
      	} else {
	        //Check all checkboxes
	        $('.mailbox-messages input[type=\'checkbox\']').prop('checked', true)
	        $('.checkbox-toggle .far.fa-square').removeClass('fa-square').addClass('fa-check-square')
      	}
      	$(this).data('clicks', !clicks)
    })

    //Handle starring for font awesome
    $('.mailbox-star').click(function (e) {
      	e.preventDefault()
      	//detect type
      	var $this = $(this).find('a > i')
      	var fa    = $this.hasClass('fa')

      	//Switch states
      	if (fa) {
        	$this.toggleClass('fa-star')
        	$this.toggleClass('fa-star-o')
      	}
    })

    /** App Menu Toggle **/ 
    $('.menu-item > a').click(function(e){
    	e.preventDefault();
    	$(this).next('.sub-menu').slideToggle();
  	});

  	/** Bootstrap Tooltip **/ 
	jQuery("a[data-toggle=tooltip], button[data-toggle=tooltip], span[data-toggle=tooltip]").tooltip();
	  $(function () {
	      $('[data-toggle="tooltip"]').tooltip()
	});
 
} );