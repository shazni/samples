function showPassword() {
    var key_attr = $('#password').attr('type');

    if(key_attr != 'text') {
        $('.checkbox').addClass('show');
        $('#password').attr('type', 'text');
    } else {
        $('.checkbox').removeClass('show');
        $('#password').attr('type', 'password');
    }
}
