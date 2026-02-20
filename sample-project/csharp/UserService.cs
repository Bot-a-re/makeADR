using System.Collections.Generic;
using System.Linq;
using SampleApp.Models;
using SampleApp.Repositories;

namespace SampleApp.Services
{
    public interface IUserService
    {
        IEnumerable<User> GetAllUsers();
        User GetUserById(int id);
        User CreateUser(User user);
    }
    
    public class UserService : IUserService
    {
        private readonly IUserRepository _repository;
        
        public UserService(IUserRepository repository)
        {
            _repository = repository;
        }
        
        public IEnumerable<User> GetAllUsers()
        {
            return _repository.GetAll();
        }
        
        public User GetUserById(int id)
        {
            return _repository.GetById(id);
        }
        
        public User CreateUser(User user)
        {
            return _repository.Add(user);
        }
    }
}
