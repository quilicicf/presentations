package jobs

import static java.nio.charset.StandardCharsets.UTF_8

userContent('custom-style/index.css', new ByteArrayInputStream('''\
  /* Style inline code blocks */
  code:not(pre code) {
    color: #e8912d;
    padding: 0 .2em;
    border-radius: .2em;
    border: solid 1px #666;
  }
  
  /* Update title */
  #jenkins-home-link {
    text-decoration: none;
  }
  
  #jenkins-name {
    display: none;
  }
  
  #jenkins-name-text {
    color: var(--header-link-color);
    vertical-align: middle;
    font-family: Georgia, serif;
    font-size: 1.8rem;
    font-weight: bolder;
    padding-left: .5rem;
  }
  
  blockquote {
    margin-left: .5em;
    padding-left: .5em;
    border-left: solid 5px #747372;
  }
  
  blockquote > p {
    color: #747372;
  }
'''.stripIndent().getBytes(UTF_8)))

userContent('custom-style/index.js', new ByteArrayInputStream('''\
  // Update title
  window.addEventListener('load', function () {
    document.querySelector('#jenkins-name-icon').remove();
    
    const linkNode = document.querySelector('#jenkins-home-link');
    const nameNode = document.createElement('span');
    nameNode.id = 'jenkins-name-text';
    nameNode.innerText = 'Jenkins My Team';
    linkNode.appendChild(nameNode);
  });
'''.stripIndent().getBytes(UTF_8)))
