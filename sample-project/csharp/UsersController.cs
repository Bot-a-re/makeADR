using System;
using System.Collections.Generic;
using Microsoft.AspNetCore.Mvc;
using SampleApp.Services;
using SampleApp.Models;

namespace SampleApp.Controllers
{
    [ApiController]
    [Route("api/[controller]")]
    public class UsersController : ControllerBase
    {
        private readonly IUserService _userService;
        
        public UsersController(IUserService userService)
        {
            _userService = userService;
        }
        
        [HttpGet]
        public ActionResult<IEnumerable<User>> GetAll()
        {
            var users = _userService.GetAllUsers();
            return Ok(users);
        }
        
        [HttpGet("{id}")]
        public ActionResult<User> GetById(int id)
        {
            var user = _userService.GetUserById(id);
            if (user == null)
            {
                return NotFound();
            }
            return Ok(user);
        }
        
        [HttpPost]
        public ActionResult<User> Create(User user)
        {
            var created = _userService.CreateUser(user);
            return CreatedAtAction(nameof(GetById), new { id = created.Id }, created);
        }
    }
}
