console.log("script file loaded");

const toggleSidebar = () => {
    if (jQuery(".sidebar").is(":visible")) {
        jQuery(".sidebar").css("display", "none");
        jQuery(".content").css("margin-left", "0%");
    } else {
        jQuery(".sidebar").css("display", "block");
        jQuery(".content").css("margin-left", "20%");
    }
};
const search=()=>{
    let query=$("#search-input").val();
    if(query==''){
        $(".search-result").hide();
    }else{
        console.log(query);
		let url=`http://localhost:8282/search/${query}`;
		fetch(url)
		.then((response) => {
			return response.json();
		})
		.then((data)=>{
			console.log(data);
			let text=`<div class='list-group'>`
			
			data.forEach((contact)=>{
				text+=`<a href='/user/${contact.cID}/contact' class='list-group-item list-group-item-action'> ${contact.email} </a>`
			});
			
			text+=`</div>`;
			$(".search-result").html(text);
			$(".search-result").show();
		});
		
      
    }
}

